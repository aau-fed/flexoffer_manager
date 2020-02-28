import { Component, OnInit, AfterViewChecked } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";

import { User } from "../shared";

import { CommonService } from "../shared/services/common.service";

import { UserService, ApiService } from "../shared/services";
import { FMANApiService } from "../shared/services/fmanapi.service";
import { moment } from "ngx-bootstrap/chronos/test/chain";
import { AlertService } from "../shared/services/alert.service";
import { BreakpointObserver } from "@angular/cdk/layout";

declare var $: any;

@Component({
  selector: "trading-kpis",
  templateUrl: "./trading-kpis.component.html",
  styleUrls: ["./trading-kpis.component.css"]
})
export class TradingKpisComponent implements OnInit {
  dateInterval = [
    moment()
      .subtract(1, "months")
      .toDate(),
    moment().toDate()
  ];
  loading = false;
  kpidata = {};
  users = [];
  selectedUser = null;

  constructor(
    private route: ActivatedRoute,
    private cService: CommonService,
    private router: Router,
    private userService: UserService,
    private fmanApiService: FMANApiService,
    private alertService: AlertService
  ) {}

  ngOnInit() {
    if (!this.userService.isLoggedIn()) {
      this.router.navigateByUrl("/login");
    }

    const currentUser = this.userService.getCurrentUser();
    this.users = [currentUser];
    if (currentUser.role === "ROLE_ADMIN") {
      this.userService.getUsers().subscribe(data => {
        this.users = [];
        this.users.push({ userName: "All users" });
        data.forEach(user => this.users.push(user));
        this.selectedUser = "All users";
      });
    }
  }

  public filterCriteriaChanged(event) {
    this.alertService.alert("KPI values are being retrieved. Please wait. ");
    this.loading = true;
    const userIndex = this.users
      .map(u => u.userName)
      .indexOf(this.selectedUser);
    const userName = userIndex >= 1 ? this.selectedUser : null;
    this.fmanApiService
      .getKPISummary(this.dateInterval[0], this.dateInterval[1], userName)
      .subscribe(
        data => {
          this.loading = false;
          this.kpidata = data;
          console.log(data);
        },
        error => {
          this.loading = false;
          this.kpidata = {};
        }
      );
  }
}
