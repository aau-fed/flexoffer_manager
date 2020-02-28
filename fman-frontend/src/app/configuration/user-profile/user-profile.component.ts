/// <reference types="@types/googlemaps" />

import { Component, OnInit, ChangeDetectorRef, AfterViewInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { FormGroup, FormBuilder, Validators, AbstractControl } from '@angular/forms';
import { first } from 'rxjs/operators';
import { MouseEvent } from '@agm/core';

import { User } from '../../shared/models/user.model';
import { AlertService } from '../../shared/services/alert.service';

import { MatDialog } from '@angular/material/dialog';
import { ConfirmationDialogComponent } from '../../shared/dialogs/confirmation-dialog/confirmation-dialog.component';
import { UserService } from '../../shared';

// google maps auto-complete
import { ElementRef, NgZone, ViewChild } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MapsAPILoader } from '@agm/core';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css']
})
export class UserProfileComponent implements OnInit {

  user: User;
  loggedInUser: User;

  userForm: FormGroup;
  loading: boolean = false;
  submitted: boolean = false;
  modified: boolean = false;
  errors: string[] = [];


  lat: number;
  lng: number;
  zoom: number = 10;

  // google maps auto-complete
  public searchControl: FormControl;
  @ViewChild("search")
  searchElementRef: ElementRef;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private alertSvc: AlertService,
    private userService: UserService,
    public dialog: MatDialog,
    private mapsAPILoader: MapsAPILoader,
    private ngZone: NgZone,
    private route: ActivatedRoute,
    private ref: ChangeDetectorRef,
    private _router: Router

  ) {

    this.loggedInUser = this.userService.getCurrentUser();

    // use FormBuilder to create a form group
    this.userForm = this.fb.group({
      'userId': [{ value: '', disabled: true }, Validators.required],
      'userName': [{ value: '', disabled: true }, Validators.required],
      'password': ['', Validators.required],
      'password_repeat': ['', Validators.required],
      'firstName': ['', Validators.required],
      'lastName': ['', Validators.required],
      'email': ['', [Validators.required, Validators.email]],
      'role': ['', Validators.required],
      'enabled': [''],
      'location': [{}, Validators.required]
    }, {
        validator: this.MatchControls('password', 'password_repeat')
      });

    // Subscribe to route parameter changes 
    this.route.params.subscribe(params => {
      if (params.userName) {
        this.userService
          .getUser(params.userName)
          .subscribe(user => {
            this.user = user;
            this.lat = this.user.location.latitude;
            this.lng = this.user.location.longitude;
            this.userForm.reset(user)
            this.userForm.controls['password_repeat'].setValue(user.password);
            this.modified = false;
          });
      }
    });

  }

  ngOnInit() {
    // monitor userForm for changes
    this.onChanges();

    // create search FormControl
    this.searchControl = new FormControl();

    // load Places Autocomplete
    this.mapsAPILoader.load().then(() => {
      let autocomplete = new google.maps.places.Autocomplete(this.searchElementRef.nativeElement, {
        types: ["address"]
      });
      autocomplete.addListener("place_changed", () => {
        this.ngZone.run(() => {
          // get the place result
          let place: google.maps.places.PlaceResult = autocomplete.getPlace();

          // verify result
          if (place.geometry === undefined || place.geometry === null) {
            return;
          }

          // set latitude, longitude and zoom
          this.lat = place.geometry.location.lat();
          this.lng = place.geometry.location.lng();
          this.modified = true;
        });
      });
    });
  }

  formatErrors(error: any) {
    this.errors = [error.message];
    this.submitted = false;
    this.ref.markForCheck();
  }

  // to check if two fields are same (e.g., password and password-repeat)
  MatchControls(firstControlName, secondControlName) {
    return (AC: AbstractControl) => {
      const firstControlValue = AC.get(firstControlName).value; // to get value in input tag
      const secondControlValue = AC.get(secondControlName).value; // to get value in input tag

      if (firstControlValue === null && secondControlValue === null) {
        return null;
      }

      if (AC.get(secondControlName).hasError('MatchFields')) {
        delete AC.get(secondControlName).errors['MatchFields'];
        AC.get(secondControlName).updateValueAndValidity();
      }

      if (firstControlValue !== secondControlValue) {
        AC.get(secondControlName).setErrors({ MatchFields: true });
      } else {
        return null;
      }
    };
  }


  get f() { return this.userForm.controls; }

  saveUser() {
    this.submitted = true;
    this.loading = true;
    const user = Object.assign(this.user, this.userForm.value);
    if (!user) {
      return;
    }

    let location = { latitude: this.lat, longitude: this.lng };
    user.location = location;

    this.userService
      .update(user)
      .subscribe(res => {
        this.alertSvc.success('user update successful');
        this.submitted = false;
        this.loading = false;
        this.modified = false;
        this.userForm.markAsPristine();
        this.userForm.markAsUntouched();

        // if the user being updated is the one logged in
        if (user.userName === this.userService.getCurrentUser().userName) {
          localStorage.setItem('user', JSON.stringify(res));
        }
        //        this.user = res;
        //        this.router.navigate(['/user-profile']);
      }, err => {
        this.formatErrors(err);
        this.modified = true;
        this.loading = false;
      })
  }

  onReset() {
    this.userForm.reset(this.user);
    this.userForm.controls['password_repeat'].setValue(this.user.password);
    this.loading = false;
    this.submitted = false;
    this.modified = false;
    this.lat = this.user.location.latitude;
    this.lng = this.user.location.longitude;
    this.router.navigate(['/user-profile', this.user.userName]);
  }

  openDeleteDialog(): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      width: '250px',
      data: {
        title: 'Delete User',
        message: 'Are you sure you want to delete the profile?'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
      if (result) {
        console.log('You chose to delete the profile');
        this.deleteUser();
      }
    });
  }

  deleteUser() {
    this.userService.deleteUserAccount(this.user.userName)
      .subscribe(
        (data: any) => {
          console.log('User account successfully deleted from server');
          this.router.navigate(['/']);
        },
        (error: any) => {
          console.log(error);
        }
      );
  }

  openDialog(): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      width: '250px',
      data: {
        title: 'Update User',
        message: 'Are you sure you want to update your profile?'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
      if (result) {
        console.log('You chose to update your profile');
        this.saveUser();
      }
    });
  }

  mapClicked($event: MouseEvent) {
    this.lat = $event.coords.lat;
    this.lng = $event.coords.lng;
    this.modified = true;
  }

  markerDragEnd($event: MouseEvent) {
    this.lat = $event.coords.lat;
    this.lng = $event.coords.lng;
    this.modified = true;
  }

  onChanges(): void {
    this.userForm.valueChanges.subscribe(_ => {
      this.modified = true;
    });
  }

  openContract(username) {
    this._router.navigate(['/user/contract', username]);
  }

  openBill(username) {
    this._router.navigate(['/user/bill', username]);
  }

}
