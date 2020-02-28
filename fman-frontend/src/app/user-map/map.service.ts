import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';


import { ApiService } from '../shared/services/api.service';

import { CommonService } from '../shared/services/common.service';
import { Router } from '@angular/router';

@Injectable()
export class MapService {
  constructor (
    private apiService: ApiService
  ) {}

  get(): Observable<any> {
    return this.apiService.get('/user/')
           .map(data => data);
  }
}
