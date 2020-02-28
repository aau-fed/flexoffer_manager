import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';


import { ApiService } from '../shared/services/api.service';
import { Headquarter } from './headquarter.model';

import { User, UserService, Profile } from '../shared';

import { CommonService } from '../shared/services/common.service';

@Injectable()
export class HeadquarterService{
  constructor (
    private apiService: ApiService
  ) {}

  get(id: string): Observable<Headquarter> {
    return this.apiService.get('/headquarter/get/' + id)
           .map(data => data);
  }

  save(item): Observable<Headquarter> {
    // If we're updating an existing item
    if (item.ID) {
      return this.apiService.put('/headquarter/update/' + item.ID, {item: item})
             .map(data => data.pci_event);

    // Otherwise, create a new item
    } else {
      return this.apiService.post('/headquarter/add', {item: item})
             .map(data => data.pci_event);
    }
  }


  getPaged(country_id): Observable<{data : Headquarter[]}>{

    return this.apiService
    .get('/headquarters/'+country_id).map(data => data);
  }

  destroy(id) {
    return this.apiService.delete('/headquarter/delete/' + id);
  }

}
