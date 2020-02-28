import { ModuleWithProviders, NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

import { UserBillComponent } from './user-bill.component';

import { AuthGuard, SharedModule } from '../shared';
import { ChartModule } from 'angular2-highcharts';
import { PaginationModule } from 'ngx-bootstrap/pagination';
import { MomentModule } from 'ngx-moment';
import { BsDatepickerModule } from 'ngx-bootstrap/datepicker';

const tradingRouting: ModuleWithProviders = RouterModule.forChild([
  {
    path: 'user/bill/:userName',
    component: UserBillComponent,
  }
]);

declare var require: any;

@NgModule({
  imports: [
    CommonModule,
    tradingRouting,
    SharedModule,
    MomentModule,
    BsDatepickerModule.forRoot(),
    ChartModule.forRoot(require('highcharts'))
  ],
  declarations: [UserBillComponent],
  providers: [
  ]
})
export class UserBillModule { }
