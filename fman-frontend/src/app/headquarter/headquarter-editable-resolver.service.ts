import { Injectable, } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, Router, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs/Rx';

import { HeadquarterService } from './headquarter.service';
import { Headquarter } from './headquarter.model';

@Injectable()
export class HeadquarterEditableResolver implements Resolve<Headquarter> {
  constructor(
    private uService: HeadquarterService,
    private router: Router,
  ) {}

  resolve(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<any> {

    return this.uService.get(route.params['id'])
           .map(
            item => {
              //console.log(item);
              return item;
             }
           )
           .catch((err) => this.router.navigateByUrl('/'));

  }
}
