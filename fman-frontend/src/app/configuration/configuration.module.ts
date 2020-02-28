import { ModuleWithProviders, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

import { ChartModule } from 'angular2-highcharts';
import { PaginationModule } from 'ngx-bootstrap/pagination';
import { TabsModule } from 'ngx-bootstrap/tabs';
import { MomentModule } from 'ngx-moment';
import { BsDatepickerModule } from 'ngx-bootstrap/datepicker';
import { AngularMultiSelectModule } from 'angular2-multiselect-dropdown/angular2-multiselect-dropdown';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

import { AgmCoreModule } from '@agm/core';
import { AgmJsMarkerClustererModule } from '@agm/js-marker-clusterer';

import { AuthGuard, SharedModule } from '../shared';
import { UserProfileComponent } from './user-profile/user-profile.component';
import { AggregatorSettingsComponent } from './aggregator-settings/aggregator-settings.component';
import { ConnectSettingsComponent } from './connect-settings/connect-settings.component';
import { AppMaterialModule } from '../app-material.module';

import { environment } from '../../environments/environment';
import { TradingConfigComponent } from './trading-config/trading-config.component';

const configurationRouting: ModuleWithProviders = RouterModule.forChild([
  {
    path: 'user-profile/:userName',
    component: UserProfileComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'aggregator-settings',
    component: AggregatorSettingsComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'connect-settings',
    component: ConnectSettingsComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'trading-config',
    component: TradingConfigComponent,
  }
]);

declare var require: any;

@NgModule({
  imports: [
    CommonModule,
    configurationRouting,
    SharedModule,
    MomentModule,
    PaginationModule.forRoot(),
    BsDatepickerModule.forRoot(),
    ChartModule.forRoot(require('highcharts')),
    AngularMultiSelectModule,
    AppMaterialModule,
    AgmCoreModule.forRoot({
      apiKey: environment.mapApiKey
    }),
    AgmJsMarkerClustererModule,
    FormsModule,
    ReactiveFormsModule,
  ],
  declarations: [
    UserProfileComponent,
    AggregatorSettingsComponent,
    ConnectSettingsComponent,
    TradingConfigComponent],
  providers: [
  ]
})

export class ConfigurationModule { }
