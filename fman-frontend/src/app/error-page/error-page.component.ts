import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { User } from '../shared';
//import { AdminService } from './admin.service';
// import { Admin } from './admin.model';


@Component({
  selector: 'app-error-page',
  templateUrl: './error-page.component.html',
  styleUrls: ['./error-page.component.css']
})
export class ErrorPageComponent implements OnInit {

  isDeleting = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
  ) {}

 // items;


  ngOnInit() {
   
  }

}
