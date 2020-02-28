import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';


import { ApiService } from '../shared/services/api.service';

import { CommonService } from '../shared/services/common.service';

@Injectable()
export class UserlistService{
  constructor (
    private apiService: ApiService
  ) {}

  get(): Observable<any> {
    return this.apiService.get('/user/')
           .map(data => data);
  }

  saveContract(username, credentials): Observable<any> {
    return this.apiService.put('/user/contract', credentials)
           .map(data => data);

  }

  getContract(username): Observable<any> {
    return this.apiService.get('/user/contract/' + username)
           .map(data => data);
  }

  getBill(username: String, date): Observable<any> {
    return this.apiService.get('/user/getbill/' + username + '/' + date)
           .map(data => data);
  }
}
