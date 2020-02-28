import { Component, OnInit } from '@angular/core';
import { UserService } from '../../shared';
import { FMANApiService } from '../../shared/services/fmanapi.service';
import { AlertService } from '../../shared/services/alert.service';
import { Router } from '@angular/router';
import { moment } from 'ngx-bootstrap/chronos/test/chain';

@Component({
  selector: 'app-aggregator-settings',
  templateUrl: './aggregator-settings.component.html',
  styleUrls: ['./aggregator-settings.component.css']
})
export class AggregatorSettingsComponent implements OnInit {
  config: any = {};

  constructor(
    private router: Router,
    private userService: UserService,
    private fmanApiService: FMANApiService,
    private alertService: AlertService) { }

  ngOnInit() {
    if (!this.userService.isLoggedIn()) {
      this.router.navigateByUrl('/login');
    }

    this.reloadConfig();
  };

  reloadConfig() {
    this.fmanApiService
    .getDashboardConfig()
    .subscribe(config => {
        this.config = config;
        this.config.optimizationFrom = moment(config.optimizationFrom).toDate();
        this.config.optimizationTo = moment(config.optimizationTo).toDate();
    })
  }

  onDashboardChange(event) {
    this.fmanApiService.setDashboardConfig({
        optimizationObjective : this.config.optimizationObjective,
        optimizationFrom : this.config.optimizationFrom,
        optimizationTo : this.config.optimizationTo,
        aggregationEnabled : this.config.aggregationEnabled,
        tradingEnabled : this.config.tradingEnabled
    })
    .subscribe(_ => {
        this.alertService.info('System settings are successfully updated.');
        this.reloadConfig();
    }, _ => {
        this.alertService.error('Error updating system settings.');
        this.reloadConfig();
     })
  }

}
