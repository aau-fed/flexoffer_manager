import { ModuleWithProviders, NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

import { MapComponent } from './map.component';

import { AuthGuard, SharedModule } from '../shared';

import { AgmCoreModule } from '@agm/core';
import { AgmJsMarkerClustererModule } from '@agm/js-marker-clusterer';

const mapRouting: ModuleWithProviders = RouterModule.forChild([
  {
    path: 'user/map',
    component: MapComponent
  }
]);

@NgModule({
  imports: [
    CommonModule,
    mapRouting,
    SharedModule,
    AgmCoreModule.forRoot({
      apiKey: 'AIzaSyBN6F_ix26wGrpAtCYRxPmPbN6G5WoFpvA'
    }),
    AgmJsMarkerClustererModule

  ],
  declarations: [
    MapComponent,
  ],
  exports: [
  ],
  providers: [
  ]
})
export class MapModule { }
