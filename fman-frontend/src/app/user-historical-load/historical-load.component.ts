import { FMANApiService } from "../shared/services/fmanapi.service";
import {
  Component,
  OnInit,
  OnDestroy,
  AfterViewInit,
  ViewChild,
  ElementRef,
  HostListener,
  ViewContainerRef
} from "@angular/core";
import { Router } from "@angular/router";
import { moment } from "ngx-bootstrap/chronos/test/chain";
import { PageChangedEvent } from "ngx-bootstrap/pagination";
import { MouseEvent } from "@agm/core";

import { UserService } from "../shared/services";
import { FlexofferModalService } from "../shared/helpers/flexoffermodal.service";
import { AlertService } from "../shared/services/alert.service";

declare var $: any;

@Component({
  selector: "app-historical-load",
  templateUrl: "./historical-load.component.html",
  styleUrls: ["./historical-load.component.css"]
})
export class HistoricalLoadComponent implements OnInit, AfterViewInit {
  loggedIn = false;
  pageSize = 10;
  currentPage = 0;
  datasetSize = 0;
  data: any[] = [];
  dateInterval = [
    moment()
      .subtract(1, "months")
      .toDate(),
    moment().toDate()
  ];

  stateList = [];
  selectedStates = [];
  dropdownSettings = {};
  users = [];
  selectedUser = null;
  currentUser = null;
  flexoffers = [];

  config: any = {};
  chart: any;
  planning_time = new Date();

  mapCenterLat: number = 1;
  mapCenterLng: number = 1;
  zoom: number = 4;

  @ViewChild("chartContainer")
  private chartContainer: ElementRef;

  @ViewChild("foview")
  private foview: ElementRef;

  chartOptions = {
    credits: {
      enabled: false
    },
    chart: {
      type: "area"
    },
    title: {
      text:
        "Default, scheduled, and market loads, available flexibility, and measurements"
    },
    xAxis: {
      // allowDecimals: false,
      type: "datetime",
      dateTimeLabelFormats: {
        // don't display the dummy year
        month: "%e. %b",
        year: "%b"
      },
      tickInterval: 15 * 60 * 1000,
      plotLines: [
        {
          color: "#FF0000", // Red
          width: 2,
          value:
            this.planning_time.getTime() -
            (this.planning_time.getTime() % (15 * 60 * 1000))
        }
      ]
    },
    yAxis: {
      title: {
        text: "Consumption Energy, kWh"
      },
      labels: {
        formatter: function() {
          return this.value;
        }
      }
    },
    tooltip: {
      // pointFormat: '<div><b>{series.name}:</b> Time interval: {point.x}; Energy amount: {point.y:,.0f};</div>',
      pointFormat:
        '<span style="color:{point.color}">\u25CF</span> {series.name}: <b>{point.y:,.3f}</b><br/>',
      shared: true
    },
    plotOptions: {
      area: {
        marker: {
          enabled: false,
          symbol: "circle",
          radius: 2,
          states: {
            hover: {
              enabled: true
            }
          }
        }
      }
    },
    time: {
      useUTC: false
    },
    series: [
      {
        id: 1,
        name: "Maximum Energy",
        type: "area",
        data: [],
        lineWidth: 0,
        fillColor: "rgba(100,190,69, 0.7)",
        marker: {
          enabled: false,
          radius: 2
        }
      },
      {
        id: 2,
        name: "Minimum Energy",
        data: [],
        type: "area",
        lineWidth: 0,
        fillColor: "rgba(100,190,69, 0.7)",
        marker: {
          enabled: false,
          radius: 2
        }
      },
      {
        id: 3,
        name: "Baseline Energy (Default schedule)",
        color: "#FFA500", // 'rgba(255, 170, 00, 0.7)'
        data: [],
        fillOpacity: 0.5,
        marker: {
          enabled: false,
          radius: 2
        }
      },
      {
        id: 4,
        name: "Scheduled Energy",
        color: "#FF0000", // 'rgba(255, 0, 0, 0.7)', // #FF0000'
        data: [],
        fillOpacity: 0.5,
        marker: {
          enabled: false,
          radius: 4
        }
      },
      {
        id: 5,
        name: "Measured Energy",
        type: "area",
        data: [],
        color: "rgba(100,100,200, 0.7)",
        fillColor: "rgba(100,100,200, 0.7)",
        marker: {
          enabled: false,
          radius: 2
        }
      },
      {
        id: 6,
        name: "Market (FMAR) Orders",
        type: "area",
        data: [],
        color: "rgba(150,0,150, 0.7)",
        fillColor: "rgba(150,0,150, 0.7)",
        marker: {
          enabled: false,
          radius: 2
        }
      }
    ]
  };

  constructor(
    private router: Router,
    private userService: UserService,
    private fmanApiService: FMANApiService,
    private foModalService: FlexofferModalService,
    private alertService: AlertService
  ) {
    this.currentUser = this.userService.getCurrentUser();
    this.selectedUser = this.currentUser.userName;
    this.users = [this.currentUser];
    if (this.currentUser.role === "ROLE_ADMIN") {
      this.userService.getUsers().subscribe(data => {
        this.users = [];
        this.users.push({ userName: "All users" });
        data.forEach(user => this.users.push(user));
        this.selectedUser = "All users";
      });
    }
  }

  ngOnInit() {
    if (!this.userService.isLoggedIn()) {
      this.router.navigateByUrl("/login");
    }

    this.getPage(0);

    this.stateList = [
      { id: 1, itemName: "Initial" },
      { id: 2, itemName: "Offered" },
      { id: 3, itemName: "Accepted" },
      { id: 4, itemName: "Rejected" },
      { id: 5, itemName: "Assigned" },
      { id: 6, itemName: "Executed" }
    ];

    this.selectedStates = [
      //      { 'id': 6, 'itemName': 'Executed' }
    ];

    this.dropdownSettings = {
      singleSelection: true,
      text: "All FlexOffer States",
      selectAllText: "Select All",
      unSelectAllText: "UnSelect All",
      classes: "myclass custom-class"
    };
  }

  onItemSelect(item: any) {
    console.log(this.selectedStates);
  }
  OnItemDeSelect(item: any) {
    console.log(this.selectedStates);
  }
  onSelectAll(items: any) {
    console.log(this.selectedStates);
  }
  onDeSelectAll(items: any) {
    console.log(this.selectedStates);
  }

  hasFoLocation(f) {
    return (
      f.locationId != null &&
      f.locationId.userLocation != null &&
      f.locationId.userLocation.longitude != null &&
      f.locationId.userLocation.latitude != null
    );
  }

  flexOffersWithLocations() {
    return this.flexoffers.filter(f => this.hasFoLocation(f));
  }

  public filterCriteriaChanged(event) {
    this.alertService.alert("Filter Criteria Changed");
    this.getPage(0);
  }

  private getPage(page: number) {
    this.currentPage = page;
    let selectedUser =
      this.selectedUser === "All users" ? null : this.selectedUser;
    this.fmanApiService
      .getFlexOfferHistory(
        page - 1,
        this.pageSize,
        selectedUser,
        this.selectedStates,
        this.dateInterval[0],
        this.dateInterval[1]
      )
      .subscribe(data => {
        this.data = data.data.flexOfferTList || [];
        this.flexoffers = [];
        this.data.forEach(item => {
          this.flexoffers.push(item.flexoffer);
        });
        this.datasetSize = data.totalCount || this.data.length;

        for (let f of this.flexoffers) {
          if (this.hasFoLocation(f)) {
            this.mapCenterLat = this.flexoffers[0].locationId.userLocation.latitude;
            this.mapCenterLng = this.flexoffers[0].locationId.userLocation.longitude;
            break;
          }
        }

        this.chart.series[0].setData(
          this.ts2chartData(data.data.overallHighSchedule)
        );
        this.chart.series[1].setData(
          this.ts2chartData(data.data.overallLowSchedule)
        );
        this.chart.series[2].setData(
          this.ts2chartData(data.data.defaultSchedule)
        );
        this.chart.series[3].setData(
          this.ts2chartData(data.data.activeSchedule)
        );
        this.chart.series[4].setData(
          this.ts2chartData(data.data.aggregatedMeasurements)
        );
        this.chart.series[5].setData(
          this.ts2chartData(data.data.marketCommitments)
        );
        this.chart.redraw(true);
      });
  }

  pageChange(event: PageChangedEvent) {
    this.getPage(event.page);
  }

  showOffer(h) {
    if (h && h.flexoffer != null) {
      this.foModalService.showFoVisualization(h.flexoffer);
    }
  }

  @HostListener("window:resize", ["$event"])
  onResize(event) {
    this.chart.setSize(
      this.chartContainer.nativeElement.clientWidth - 20,
      this.chartContainer.nativeElement.clientHeight - 20
    );
  }

  saveInstance(chartInstance) {
    this.chart = chartInstance;
  }

  private ts2chartData(ts) {
    if (!ts) {
      return [];
    }

    const startDate = moment(ts.startTime).toDate(); // new Date();
    const startInterval = ts.startInterval;

    const res = new Array(ts.intervals.length * 2);
    for (let i = 0; i < ts.intervals.length; i++) {
      const dtFrom = new Date(
        startDate.getTime() +
          1000 * (ts.intervals[i] - startInterval) * ts.numSecondsPerInterval
      );
      const dtTo = new Date(
        startDate.getTime() +
          1000 *
            (ts.intervals[i] - startInterval + 1) *
            ts.numSecondsPerInterval
      );

      if (ts.data[i] > 1e308) {
        res[2 * i] = [dtFrom.getTime(), null];
        res[2 * i + 1] = [dtTo.getTime() - 1, null];
      } else {
        res[2 * i] = [dtFrom.getTime(), ts.data[i]];
        res[2 * i + 1] = [dtTo.getTime() - 1, ts.data[i]];
      }
    }
    return res;
  }

  ngAfterViewInit() {
    this.onResize(null);
  }

  mapClicked($event: MouseEvent) {}

  onMouseOver(infoWindow, gm) {
    if (gm.lastOpen != null) {
      gm.lastOpen.close();
    }

    gm.lastOpen = infoWindow;

    infoWindow.open();
  }

  foTabSelected(event) {
    let fos = this.flexoffers;
    this.flexoffers = [];

    setTimeout(() => (this.flexoffers = fos), 0);
  }
}
