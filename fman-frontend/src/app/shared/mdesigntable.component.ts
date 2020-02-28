import { Component, Input, OnInit  } from '@angular/core';
import { Http, Response } from '@angular/http';
import { MdesignTable } from './models/mdesigntable.model';
@Component({
  selector: 'mdesigntable',
  templateUrl: './mdesigntable.component.html',
  styleUrls: []
})
export class MdesignTableComponent{

  tables: any;

  constructor(private http: Http) {
//    this.tables = 
      // {
      //   COLUMN:['FirstName','LastName'],
      //   DATA:[
      //     [
      //       'John',
      //       'Doe'
      //     ],
      //     [
      //       'Jane',
      //       'Smith',
      //     ]
      //   ],
      // };
   }

   ngOnInit() {
    this.loadTablesData();
    }

    loadTablesData() {
    this.tables = this.http.get("../../../api/tables.php")
                .map(res => res.json())
                .do(data => console.log(data));
  }


}
