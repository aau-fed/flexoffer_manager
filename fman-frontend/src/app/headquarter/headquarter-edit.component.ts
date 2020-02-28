import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormControl } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { HeadquarterService } from './headquarter.service';
import { Headquarter } from './headquarter.model';

import { UserService } from '../shared/services';

declare var $: any;

@Component({
  selector: 'app-headquarter-edit',
  templateUrl: './headquarter-edit.component.html',
  styleUrls: ['./headquarter-edit.component.css']
})
export class HeadquarterEditComponent implements OnInit {

  headquarter: Headquarter = new Headquarter();
  uForm: FormGroup;
  tagField = new FormControl();
  errors: Object = {};
  isSubmitting = false;
  new_mode = false;

  constructor(
    private uService: HeadquarterService,
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private userService: UserService
    ) {
    // use the FormBuilder to create a form group
    this.uForm = this.fb.group({
      ID: '',
      HEADQUARTERS_NAME: '',
    });
    // Optional: subscribe to value changes on the form
    // this.articleForm.valueChanges.subscribe(value => this.updateArticle(value));
  }

  ngOnInit() {

    this.userService.currentUser.subscribe(
      (userData) => {
  
        // if(userData.authority_name != 'TMC_ADMIN')
        // {
        //   this.router.navigateByUrl('/error');
        // }
        
      }
    );


    this.route.data.subscribe(
      (data) => {
        if (data.headquarter !== undefined) {
          this.headquarter = data.headquarter;
          this.uForm.patchValue(this.headquarter['headquarter']);

          this.new_mode = true;
        }
      }
    );
  }

  submitForm() {
    this.isSubmitting = true;

    // update the model
    this.updateItem(this.uForm.value);

    // post the changes
    this.uService
      .save(this.headquarter)
      .subscribe(
        //item => this.router.navigateByUrl('/pcievent/' + item.id),
        item => this.router.navigateByUrl('/headquarters'),
        err => {
          this.errors = err;
          this.isSubmitting = false;
        }
      );
  }

  dialog_title;
  dialog_body;
  dialog_mode = '';
  item: Headquarter = new Headquarter();
  delete(o: Headquarter) {
    // alert(o.ID);
    this.item = o;
    this.dialog_mode = 'DELETE';
    this.dialog_title = "dialog_title";
    this.dialog_body = "delete_question";
    $('#dialogbox').modal('show');
  }

  update(o: Headquarter) {
    // alert(o.ID);
    this.item = o;
    this.dialog_mode = 'UPDATE';
    this.dialog_title = "dialog_title";
    this.dialog_body = "save_question";
    $('#dialogbox').modal('show');
  }

  cancel() {
    this.dialog_mode = 'CANCEL';
    this.dialog_title = "dialog_title";
    this.dialog_body = "cancel_question";

    $('#dialogbox').modal('show');
  }

  executeModalAction() {
    if (this.dialog_mode == 'UPDATE') {
      this.submitForm();
    }
    else
      if (this.dialog_mode == 'DELETE') {
        // this.delete(this.item);
        this.isSubmitting = true;

        this.uService.destroy(this.headquarter['headquarter'].ID)
          .subscribe(
            success => {
              this.router.navigateByUrl('/headquarters');
            }
          );
      }
      else
        if (this.dialog_mode == 'CANCEL') {
          this.back();
        }
  }


  updateItem(values: Object) {
    (<any>Object).assign(this.headquarter, values);
  }

  back() {
    this.router.navigateByUrl('/headquarters');
  }

}
