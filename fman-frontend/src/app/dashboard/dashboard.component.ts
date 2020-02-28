import {
  Component,
  OnInit,
  AfterViewInit,
  ViewChild,
  ElementRef,
  HostListener
} from "@angular/core";
import { Router } from "@angular/router";

import { UserService } from "../shared/services";
import { FMANApiService } from "../shared/services/fmanapi.service";
import { zip } from "d3";
import { moment } from "ngx-bootstrap/chronos/test/chain";
import { AlertService } from "../shared/services/alert.service";

declare var $: any;

@Component({
  selector: "app-dashboard",
  templateUrl: "./dashboard.component.html"
})
export class DashboardComponent implements OnInit, AfterViewInit {
  loggedIn = false;

  data: any = {};
  chart: any;
  planning_time = new Date();

  @ViewChild("chartContainer")
  private chartContainer: ElementRef;

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
        lineWidth: 1,
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
        lineWidth: 1,
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
    private alertService: AlertService
  ) {}

  ngOnInit() {
    if (!this.userService.isLoggedIn()) {
      this.router.navigateByUrl("/login");
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

  private updateDashboard() {
    /* Get dashboard details */
    this.fmanApiService.getDashboardContent().subscribe(data => {
      this.data = data;
      this.chart.series[0].setData(this.ts2chartData(data.overallHighSchedule));
      this.chart.series[1].setData(this.ts2chartData(data.overallLowSchedule));
      this.chart.series[2].setData(this.ts2chartData(data.defaultSchedule));
      this.chart.series[3].setData(this.ts2chartData(data.activeSchedule));
      this.chart.series[4].setData(
        this.ts2chartData(data.aggregatedMeasurements)
      );
      this.chart.series[5].setData(this.ts2chartData(data.marketCommitments));
      //            this.chart.xAxis.plotLines[0].value = (new Date((data.currentPlanningInterval) * 1000 * 15 * 60)).getTime()
    });
  }

  ngAfterViewInit() {
    this.updateDashboard();
    this.onResize(null);
  }
}
