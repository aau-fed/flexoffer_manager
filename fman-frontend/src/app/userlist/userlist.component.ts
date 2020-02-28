import { UserService } from '../shared/services/user.service';
import { UserlistService } from './userlist.service';
import { ChangeDetectorRef, Component, OnInit, TemplateRef} from '@angular/core';
import { User } from '../shared/models';
import { FormBuilder, FormGroup, FormControl, Validators } from '@angular/forms';

import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Router } from '@angular/router';
@Component({
  selector: 'app-user-list',
  templateUrl: './userlist.component.html',
  styleUrls: ['./userlist.component.css']
})
export class UserListComponent implements OnInit {

  public prosumers: User[] = [];
  public sys_users: User[] = [];

  constructor(
    public userService: UserService,
    private _router: Router
    ) { }

  ngOnInit() {
    this.userService
        .getUsers()
        .subscribe(data => {
          this.prosumers = data.filter(u => u.role === 'ROLE_PROSUMER');
          this.sys_users = data.filter(u => u.role !== 'ROLE_PROSUMER');
        })
  }
 
  openContract(username) {
    this._router.navigate(['/user/contract', username]);
  }

  openBill(username) {
    this._router.navigate(['/user/bill', username]);
  }

}
