import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs/Rx';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';


import { ApiService } from '../shared/services/api.service';
import { Dashboard } from './dashboard.model';

import { User, UserService, Profile } from '../shared';

import { CommonService } from '../shared/services/common.service';

@Injectable()
export class DashboardService {
  constructor (
    private router: Router,
    private userService: UserService
  ) {}

   ngOnInit() {

    if (!this.userService.isLoggedIn()){
       this.router.navigateByUrl('/login');
    }

  }

}
