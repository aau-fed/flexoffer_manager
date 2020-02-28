import { Injectable } from '@angular/core';
import { URLSearchParams } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

import { ApiService } from './api.service';

import { KeyVal } from '../models/keyval.model';

import { TranslateService } from '../../translate';

@Injectable()
export class CommonService {

  constructor (
    private apiService: ApiService,
    private translate: TranslateService
  ) {}

  launchWIndow(route_x) {
      window.open(this.apiService.getUrlLink(route_x));
  }


  makeLink(route_x)
  {
    return this.apiService.getUrlLink(route_x);
  }

  getMonths(): KeyVal[]{
    var m: Array<KeyVal> = [];
    m.push({'key': '1', 'value': this.translate.instant('january')});
    m.push({'key': '2', 'value': this.translate.instant('february')});
    m.push({'key': '3', 'value': this.translate.instant('march')});
    m.push({'key': '4', 'value': this.translate.instant('april')});
    m.push({'key': '5', 'value': this.translate.instant('may')});
    m.push({'key': '6', 'value': this.translate.instant('june')});
    m.push({'key': '7', 'value': this.translate.instant('july')});
    m.push({'key': '8', 'value': this.translate.instant('august')});
    m.push({'key': '9', 'value': this.translate.instant('september')});
    m.push({'key': '10', 'value': this.translate.instant('october')});
    m.push({'key': '11', 'value': this.translate.instant('november')});
    m.push({'key': '12', 'value': this.translate.instant('december')});
    return m;
  }


  getDBs(): KeyVal[]{
    var m: Array<KeyVal> = [];
    m.push({'key': 'db_all', 'value': this.translate.instant('all_dbs')});
    m.push({'key': 'db1', 'value': this.translate.instant('db1')});
    m.push({'key': 'db2', 'value': this.translate.instant('db2')});
    m.push({'key': 'db3', 'value': this.translate.instant('db3')});
    m.push({'key': 'db4', 'value': this.translate.instant('db4')});
    m.push({'key': 'db5', 'value': this.translate.instant('db5')});
    m.push({'key': 'db6', 'value': this.translate.instant('db6')});
    m.push({'key': 'db7', 'value': this.translate.instant('db7')});
    m.push({'key': 'db8', 'value': this.translate.instant('db8')});
    return m;
  }


  getDays(month,year): KeyVal[]{
      var m: Array<KeyVal> = [];
      var days = this.getDaysInMonth(month,year);
      for(var i = 1 ; i < days ; i++)
      {
        m.push({'key': i.toString(), 'value': i.toString()});
      }
      return m;
  }


  getDaysInMonth = function(month,year) {
        // Here January is 1 based
        //Day 0 is the last day in the previous month
      return new Date(year, month, 0).getDate();
      // Here January is 0 based
      // return new Date(year, month+1, 0).getDate();
  };


  getYears(): KeyVal[]{
    var currentYear = new Date().getFullYear();
    var m: Array<KeyVal> = [];
    var startYear = currentYear - 25;

    while ( startYear <= currentYear ) {
        m.push({'key': startYear.toString(), 'value': startYear.toString()});
        startYear++;
    } 

    return m;
  }



  getCol5s(): KeyVal[]{
    var m: Array<KeyVal> = [];
    m.push({'key': 'col_5_accessory_sales', 'value': this.translate.instant('col_5_accessory_sales')});
    m.push({'key': 'col_5_sales_unit', 'value': this.translate.instant('col_5_sales_unit')});
    m.push({'key': 'col_5_process_requirement', 'value': this.translate.instant('col_5_process_requirement')});
    return m;
  }

  getCol6s(): KeyVal[]{
    var m: Array<KeyVal> = [];
    m.push({'key': 'col_6_sales_code', 'value': this.translate.instant('col_6_sales_code')});
    m.push({'key': 'col_6_carmodel', 'value': this.translate.instant('col_6_carmodel')});
    m.push({'key': 'col_6_item_category', 'value': this.translate.instant('col_6_item_category')});
    m.push({'key': 'col_6_sales_classification', 'value': this.translate.instant('col_6_sales_classification')});
    m.push({'key': 'col_6_vehicle_sales', 'value': this.translate.instant('col_6_vehicle_sales')});
    m.push({'key': 'col_6_gen_non_gen', 'value': this.translate.instant('col_6_gen_non_gen')});
    m.push({'key': 'col_6_unit_package', 'value': this.translate.instant('col_6_unit_package')});
    m.push({'key': 'col_6_item_no', 'value': this.translate.instant('col_6_item_no')});
    m.push({'key': 'col_6_sales_unit', 'value': this.translate.instant('col_6_sales_unit')});
    m.push({'key': 'col_6_process_requirement', 'value': this.translate.instant('col_6_process_requirement')});
    return m;
  }

}
