import { ModuleWithProviders, NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

import { ErrorPageComponent } from './error-page.component';
// import { PciEventEditComponent } from './pci-user-edit.component';

// import { PciUserResolver } from './pci-user-resolver.service';
// import { PciUserEditableResolver } from './pci-user-editable-resolver.service';
import { AuthGuard, SharedModule } from '../shared';


const errorpageRouting: ModuleWithProviders = RouterModule.forChild([
  // {
  //   path: 'distributormenus',
  //   component: PciEventComponent,
  //   canActivate: [AuthGuard],
  //   resolve: {
  //     pci_events: PciUserResolver
  //   }
  // },
  {
    path: 'error',
    canActivate: [AuthGuard],
    component: ErrorPageComponent
  },
  // {
  //   path: 'distributormenu/:id',
  //   canActivate: [AuthGuard],
  //   component: PciEventEditComponent,
  //   resolve: {
  //     pci_event: PciUserEditableResolver
  //   }
  // }
]);

@NgModule({
  imports: [
    CommonModule,
    errorpageRouting,
    SharedModule
  ],
  declarations: [ErrorPageComponent],
  // providers: [
  //   PciUserResolver, PciUserEditableResolver
  // ]
})
export class ErrorPageModule { }
