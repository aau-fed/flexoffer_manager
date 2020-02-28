import { Injectable, } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, Router, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs/Rx';

import { HeadquarterService } from './headquarter.service';
import { Headquarter } from './headquarter.model';

@Injectable()
export class HeadquarterResolver implements Resolve<{data : Headquarter[]}> {
  constructor(
    private uService: HeadquarterService
  ) {}

  resolve(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<{data : Headquarter[]}> {
    //return null;
    return this.uService.getPaged(-1);
  }
}
