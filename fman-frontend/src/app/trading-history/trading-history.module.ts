import { ModuleWithProviders, NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

import { TradingHistoryComponent } from './trading-history.component';

import { AuthGuard, SharedModule } from '../shared';
import { ChartModule } from 'angular2-highcharts';
import { PaginationModule } from 'ngx-bootstrap/pagination';
import { TabsModule } from 'ngx-bootstrap/tabs';
import { MomentModule } from 'ngx-moment';
import { BsDatepickerModule } from 'ngx-bootstrap/datepicker';

const tradingRouting: ModuleWithProviders = RouterModule.forChild([
  {
    path: 'trading-history',
    component: TradingHistoryComponent,
  }
]);

declare var require: any;

@NgModule({
  imports: [
    CommonModule,
    tradingRouting,
    SharedModule,
    MomentModule,
    TabsModule.forRoot(),
    PaginationModule.forRoot(),
    BsDatepickerModule.forRoot(),
    ChartModule.forRoot(require('highcharts'))
  ],
  declarations: [TradingHistoryComponent],
  providers: [
  ]
})
export class TradingHistoryModule { }
