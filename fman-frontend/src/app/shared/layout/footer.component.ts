import { Component , DoCheck} from '@angular/core';

import { User } from '../models';
import { UserService } from '../services';

@Component({
  selector: 'layout-footer',
  templateUrl: './footer.component.html'
})
export class FooterComponent implements DoCheck {
  today: number = Date.now();

  loggedIn = false;

  constructor(
    private userService: UserService
  ) {}

  currentUser: User;

  ngDoCheck(){
    this.loggedIn = this.userService.isLoggedIn();
  }

  ngOnInit() {
    this.loggedIn = this.userService.isLoggedIn();
  }
}