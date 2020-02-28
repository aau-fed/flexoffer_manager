import { AlertService } from './../shared/services/alert.service';
import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, FormControl, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Rx';

import { UserService } from '../shared';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'auth-page',
  templateUrl: './auth.component.html'
})
export class AuthComponent implements OnInit {
  authType: String = '';
  title: String = '';
  errors: Array<string> = [];
  isSubmitting = false;
  authForm: FormGroup;
  signupForm: FormGroup;
  hasError = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private userService: UserService,
    private fb: FormBuilder,
    private ref: ChangeDetectorRef,
    private alertService: AlertService
  ) {
    // use FormBuilder to create a form group
    this.authForm = this.fb.group({
      'userName': ['', Validators.required],
      'password': ['', Validators.required]
    });

    this.signupForm = this.fb.group({
      'firstName': ['', Validators.required],
      'lastName': ['', Validators.required],
      'email': ['', [Validators.required, Validators.email]],
      'userName': ['', Validators.required],
      'password': ['', Validators.required]
      // 'term': [false, Validators.required]
    });
  }

  ngOnInit() {

    if(this.userService.isLoggedIn() && this.authType == 'login'){
      this.router.navigateByUrl('/dashboard');
    }

    this.route.url.subscribe(data => {
      // Get the last piece of the URL (it's either 'login' or 'register')
      this.authType = data[data.length - 1].path;
      // Set a title for the page accordingly
      this.title = (this.authType === 'login') ? 'Sign in' : 'Sign up';
      // add form control for username if this is the register page
      // if (this.authType === 'register') {
      //   this.authForm.addControl('username', new FormControl());
      // }
    });
  }

  formatErrors(error: any) {
      this.errors = [error.message || 'Sign-in error'];
      this.isSubmitting = false;
      this.hasError = true;
      this.ref.markForCheck();
  }

  submitLoginForm() {
    this.isSubmitting = true;
    // this.hasError = false;
    const credentials = this.authForm.value;
    this.userService
    .attemptAuth(this.authType, credentials)
    .subscribe(
      (data) => {
        this.router.navigateByUrl('/dashboard');
      },
      err => {
        this.formatErrors(err);
     }
    );
  }

  submitSignUpForm() {
    this.isSubmitting = true;
    const form_data = this.signupForm.value;
    this.userService
    .attemptSignupOnBehalfOfAdmin(form_data)
    .subscribe(
      (data) => {
        this.alertService.info('User successfully added');
        let router = this.router;

        setTimeout(function() {
          router.navigateByUrl('/user/list');
        }, 2000);

      },
      err => {
        this.alertService.error('Error adding the user!');
         this.formatErrors(err);
     }
    );
  }

}
