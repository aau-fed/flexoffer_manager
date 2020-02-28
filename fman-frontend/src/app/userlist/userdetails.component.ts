import { UserService } from '../shared/services/user.service';
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { User } from '../shared/models';

@Component({
  selector: 'app-user-details',
  templateUrl: './userdetails.component.html',
  styleUrls: ['./userdetails.component.css']
})
export class UserDetailComponent implements OnInit {

  public user: User = null;
  public isSubmitting = false;
  public userForm: FormGroup;
  public errors: string[] = [];

  constructor(private route: ActivatedRoute,
              public userService: UserService,
              private fb: FormBuilder,
              private ref: ChangeDetectorRef) {
 
         // use FormBuilder to create a form group
        this.userForm = this.fb.group({
            'userId' :  [{value: '', disabled: true}, Validators.required],
            'userName': [{value: '', disabled: true}, Validators.required],
            'password': ['', Validators.required],
            'firstName': ['', Validators.required],
            'lastName': ['', Validators.required],
            'email': ['', [Validators.required, Validators.email]],
            'role': ['', Validators.required],
            'enabled': ['']
        });

               // Subscribe to route parameter changes 
        this.route.params.subscribe( params => {
                if (params.userName) {
                    this.userService
                        .getUser(params.userName)
                        .subscribe(user  =>  {
                            this.user = user;
                            this.userForm.reset(user)
                        });
                }
            });
    };

    ngOnInit() {}

    formatErrors(error: any) {
        this.errors = [error.message];
        this.isSubmitting = false;
        this.ref.markForCheck();
    }

    saveUser() {
        this.isSubmitting = true;
        const user = Object.assign(this.user, this.userForm.value);
        if (!user) {
            return;
        }
        this.userService
            .update(user)
            .subscribe(res => {
                this.isSubmitting = false;
                this.userForm.markAsPristine();
                this.userForm.markAsUntouched();
            }, err => {
                this.formatErrors(err);
            })
    }

}
