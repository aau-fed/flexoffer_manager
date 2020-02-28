import { ModuleWithProviders, NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

import { DashboardComponent } from './dashboard.component';

import { AuthGuard, SharedModule } from '../shared';
import { ChartModule } from 'angular2-highcharts';
import { MomentModule } from 'ngx-moment';
import { BsDatepickerModule } from 'ngx-bootstrap/datepicker';


const dashboardRouting: ModuleWithProviders = RouterModule.forChild([
  {
    path: 'dashboard',
    component: DashboardComponent,
  },
  {
    path: '',
    component: DashboardComponent,
  }
]);

declare var require: any;

@NgModule({
  imports: [
    CommonModule,
    dashboardRouting,
    SharedModule,
    MomentModule,
    BsDatepickerModule.forRoot(),
    ChartModule.forRoot(require('highcharts'))
  ],
  declarations: [DashboardComponent],
  providers: [
  ]
})
export class DashboardModule { }
