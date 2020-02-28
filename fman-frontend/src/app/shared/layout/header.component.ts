import { Component, OnInit, DoCheck } from '@angular/core';

import { Router, ActivatedRoute } from '@angular/router';

import { User } from '../models';
import { UserService } from '../services';

declare var $: any;

@Component({
  selector: 'layout-header',
  templateUrl: './header.component.html'
})
export class HeaderComponent implements OnInit, DoCheck {

  activemenu = '';
  loggedIn = false;
  no_upload = false;

  showSidebar = true;
  currentUser: User;
  userName = '';

  constructor(
    private userService: UserService,
    private router: Router,
    private route: ActivatedRoute
  ) {

  }

  ngDoCheck() {
    this.loggedIn = this.userService.isLoggedIn();
  }


  ngOnInit() {
    if (this.userService.isLoggedIn()) {
      const user = this.userService.getCurrentUser();
      this.userName = user.userName;
    }
  }
}
