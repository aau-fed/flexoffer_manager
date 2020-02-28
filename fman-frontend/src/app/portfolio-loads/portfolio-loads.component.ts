import { Component, OnInit, AfterViewChecked } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";

import { User } from "../shared";
import { PortfolioLoadsService } from "./portfolio-loads.service";

import { CommonService } from "../shared/services/common.service";

import { UserService, ApiService } from "../shared/services";
import { FMANApiService } from "../shared/services/fmanapi.service";

declare var $: any;

@Component({
  selector: "portfolio-loads",
  templateUrl: "./portfolio-loads.component.html",
  styleUrls: ["./portfolio-loads.component.css"]
})
export class PortfolioLoadsComponent implements OnInit {
  data: any[] = [];
  userFOs = [];
  portfolioFOs = [];
  loading_portfolio = false;
  activePortfolio = null;

  constructor(
    private route: ActivatedRoute,
    private aService: PortfolioLoadsService,
    private cService: CommonService,
    private router: Router,
    private userService: UserService,
    private fmanApiService: FMANApiService
  ) {}

  ngOnInit() {
    if (!this.userService.isLoggedIn()) {
      this.router.navigateByUrl("/login");
    }

    this.getPortfolioLoads();
  }

  getPortfolioLoads() {
    this.loading_portfolio = true;

    this.fmanApiService.getActivePortfolio().subscribe(res => {
      this.activePortfolio = res;
    });

    this.fmanApiService.getOptimizedPortfolioFlexOffers().subscribe(
      data => {
        this.loading_portfolio = false;
        this.portfolioFOs = data;
      },
      error => {
        this.loading_portfolio = false;
      }
    );
  }
}
