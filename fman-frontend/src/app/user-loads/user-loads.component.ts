import { Component, OnInit, AfterViewChecked } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";

import { User } from "../shared";
import { UserLoadService } from "./user-loads.service";

import { CommonService } from "../shared/services/common.service";

import { UserService, ApiService } from "../shared/services";
import { FMANApiService } from "../shared/services/fmanapi.service";

declare var $: any;

@Component({
  selector: "app-user-loads",
  templateUrl: "./user-loads.component.html",
  styleUrls: ["./user-loads.component.css"]
})
export class UserLoadsComponent implements OnInit {
  data: any[] = [];
  userFOs = [];
  portfolioFOs = [];

  constructor(
    private route: ActivatedRoute,
    private aService: UserLoadService,
    private cService: CommonService,
    private router: Router,
    private userService: UserService,
    private fmanApiService: FMANApiService
  ) {}

  ngOnInit() {
    if (!this.userService.isLoggedIn()) {
      this.router.navigateByUrl("/login");
    }

    this.getUserLoads();
  }

  getUserLoads() {
    this.fmanApiService.getUserFlexOffers().subscribe(data => {
      this.userFOs = data.map(d => d.flexoffer);
    });
  }
}
