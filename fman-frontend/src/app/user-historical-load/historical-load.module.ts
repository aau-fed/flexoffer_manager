import { ModuleWithProviders, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

import { HistoricalLoadComponent } from './historical-load.component';
import { AuthGuard, SharedModule } from '../shared';

import { ChartModule } from 'angular2-highcharts';
import { PaginationModule } from 'ngx-bootstrap/pagination';
import { TabsModule } from 'ngx-bootstrap/tabs';
import { MomentModule } from 'ngx-moment';
import { BsDatepickerModule } from 'ngx-bootstrap/datepicker';
import { AngularMultiSelectModule } from 'angular2-multiselect-dropdown/angular2-multiselect-dropdown';

import { AgmCoreModule } from '@agm/core';
import { AgmJsMarkerClustererModule } from '@agm/js-marker-clusterer';

const historicalLoadRouting: ModuleWithProviders = RouterModule.forChild([
  {
    path: 'historical-loads',
    component: HistoricalLoadComponent
  }
]);

declare var require: any;

@NgModule({
  imports: [
    CommonModule,
    historicalLoadRouting,
    SharedModule,
    MomentModule,
    TabsModule.forRoot(),
    PaginationModule.forRoot(),
    BsDatepickerModule.forRoot(),
    ChartModule.forRoot(require('highcharts')),
    AngularMultiSelectModule,
    AgmCoreModule.forRoot({
      apiKey: 'AIzaSyBN6F_ix26wGrpAtCYRxPmPbN6G5WoFpvA'
    }),
    AgmJsMarkerClustererModule,

  ],
  declarations: [HistoricalLoadComponent],
  providers: [
  ]
})
export class HistoricalLoadModule { }
