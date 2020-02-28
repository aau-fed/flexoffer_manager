import { ModuleWithProviders, NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

import { HeadquarterComponent } from './headquarter.component';
import { HeadquarterEditComponent } from './headquarter-edit.component';

import { HeadquarterResolver } from './headquarter-resolver.service';
import { HeadquarterEditableResolver } from './headquarter-editable-resolver.service';
import { AuthGuard, SharedModule } from '../shared';



const headquarterRouting: ModuleWithProviders = RouterModule.forChild([
  {
    path: 'headquarters',
    component: HeadquarterComponent,
    // canActivate: [AuthGuard],
    resolve: {
      headquarters: HeadquarterResolver
    }
  },
  {
    path: 'headquarter',
    // canActivate: [AuthGuard],
    component: HeadquarterEditComponent
  },
  {
    path: 'headquarter/:id',
    // canActivate: [AuthGuard],
    component: HeadquarterEditComponent,
    resolve: {
      headquarter: HeadquarterEditableResolver
    }
  }
]);

@NgModule({
  imports: [
    CommonModule,
    headquarterRouting,
    SharedModule
  ],
  declarations: [HeadquarterComponent, HeadquarterEditComponent],
  providers: [
    HeadquarterResolver, HeadquarterEditableResolver
  ]
})
export class HeadquarterModule { }
