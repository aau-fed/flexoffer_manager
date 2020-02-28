import { Component, OnInit } from "@angular/core";
import { Router } from "@angular/router";

import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { UserService } from "../../shared";
import { FMANApiService } from "../../shared/services/fmanapi.service";
import { AlertService } from "../../shared/services/alert.service";

declare var $: any;

@Component({
  selector: "app-trading-config",
  templateUrl: "./trading-config.component.html"
})
export class TradingConfigComponent implements OnInit {
  loggedIn = false;
  configForm: FormGroup;

  constructor(
    private router: Router,
    private userService: UserService,
    private fmanApiService: FMANApiService,
    private formBuilder: FormBuilder,
    private alertService: AlertService
  ) {}

  ngOnInit() {
    if (!this.userService.isLoggedIn()) {
      this.router.navigateByUrl("/login");
    }

    this.configForm = this.formBuilder.group({
      tradingEnabled: [null, Validators.required],
      tradingHorizonInMultiplesOf15min: [1, Validators.required],
      tradingFrequencyInMultiplesOf15min: [1, Validators.required],
      proportionalBidMargin: [1.05, Validators.required],
      bidMarginPerDeltaKwh: [0.05, Validators.required],
      bidUpdatePolicyOnAbsoluteEnergyChange: [500, Validators.required],
      contractImbalanceFee: [1e6, Validators.required],
      contractCutOffAmount: [1.0, Validators.required]
    });

    this.fmanApiService.getTradingConfig().subscribe(conf => {
      this.configForm.patchValue(conf);
    });
  }

  // convenience getter for easy access to form fields
  get f() {
    return this.configForm.controls;
  }

  onSubmit() {
    // stop here if form is invalid
    if (this.configForm.invalid) {
      this.alertService.error("The form is invalid.");
      return;
    }

    this.fmanApiService
      .saveTradingConfig(this.configForm.value)
      .subscribe(d => {
        this.alertService.info("Settings successfully saved.");
        this.configForm.markAsPristine();
      });
  }
}
