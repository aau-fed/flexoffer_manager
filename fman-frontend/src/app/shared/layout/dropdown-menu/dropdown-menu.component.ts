import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmationDialogComponent } from '../../dialogs/confirmation-dialog/confirmation-dialog.component';
import { UserService } from '../../services';
import { Router } from '@angular/router';

@Component({
  selector: 'app-dropdown-menu',
  templateUrl: './dropdown-menu.component.html',
  styleUrls: ['./dropdown-menu.component.css']
})
export class DropdownMenuComponent implements OnInit {

  public userName: string;

  constructor(
    private userService: UserService,
    public dialog: MatDialog,
    private router: Router

    ) { }

  ngOnInit() {
    this.userService
        .getCurrentUserObservable()
        .subscribe(user => {
          this.userName = user != null ? user.userName : '';
        });
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
