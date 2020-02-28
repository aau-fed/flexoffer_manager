import { UserDetailComponent } from './userdetails.component';
import { ModuleWithProviders, NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

import { UserListComponent } from './userlist.component';

import { AuthGuard, SharedModule } from '../shared';
import { BsDatepickerModule } from 'ngx-bootstrap/datepicker';

const userRouting: ModuleWithProviders = RouterModule.forChild([
  {
    path: 'user/list',
    component: UserListComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'user/list/:userName',
    component: UserDetailComponent,
    canActivate: [AuthGuard]
  }

]);

@NgModule({
  imports: [
    CommonModule,
    userRouting,
    SharedModule
  ],
  declarations: [UserListComponent, UserDetailComponent],
  providers: [  ]
})
export class UserListModule { }
