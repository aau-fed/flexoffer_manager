import { Component, OnInit, DoCheck } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmationDialogComponent } from '../../shared/dialogs/confirmation-dialog/confirmation-dialog.component';
import { User } from '../models';
import { UserService } from '../services';
import { Router } from '@angular/router';

@Component({
  selector: 'layout-nav',
  templateUrl: './nav.component.html'
})
export class NavComponent implements OnInit, DoCheck {
  loggedIn = false;
  userName: String;

  constructor(
    private userService: UserService,
    public dialog: MatDialog,
    private router: Router
    ) {}

  ngDoCheck() {
    this.loggedIn = this.userService.isLoggedIn();
  }

  ngOnInit() {
    let user = this.userService.getCurrentUser();
    this.userName = user != null ? user.userName : '';
  }

  logout() {
    this.userService.purgeAuth();
    this.router.navigate(['/login']);
  }


  openDialog(): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      width: '250px',
      data: {
        title: "Logout User",
        message: "Are you sure you want to log out?"
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
      if (result) {
        console.log('You chose to log out');
        this.userService.purgeAuth();
        this.router.navigate(['/login']);
      }
    });
  }


}
