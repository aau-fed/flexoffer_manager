import { Injectable } from "@angular/core";
import { Observable } from "rxjs/Rx";
import "rxjs/add/operator/map";
import "rxjs/add/operator/catch";

import { ApiService } from "../shared/services/api.service";

import { CommonService } from "../shared/services/common.service";

@Injectable()
export class PortfolioLoadsService {
  constructor(private apiService: ApiService) {}

  getDevices(): Observable<any> {
    // return this.apiService.getkpi('/user/')
    //        .map(data => data);
    return;
  }
}
