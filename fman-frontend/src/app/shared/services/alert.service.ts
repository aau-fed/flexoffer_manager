import { Injectable, OnInit } from '@angular/core';
import { NotificationsService } from 'angular2-notifications';
import { FormBuilder, FormGroup } from '@angular/forms';

@Injectable({
  providedIn: 'root'
})
export class AlertService implements OnInit {

  form: FormGroup;

  constructor(
    private _notifications: NotificationsService,
    private _fb: FormBuilder
  ) {
    this.form = this._fb.group({
      timeOut: 5000,
      showProgressBar: true,
      pauseOnHover: false,
      clickToClose: true,
      animate: 'fromRight' // {'fromRight', 'fromLeft', 'scale', 'rotate'}
    });
  }

  ngOnInit() {
  }

  success(content: string) {
    this._notifications.success('Success', content, this.form.getRawValue());
  }

  error(content: string) {
    this._notifications.error('Error', content, this.form.getRawValue());
  }

  info(content: string) {
    this._notifications.info('Info', content, this.form.getRawValue());
  }

  warn(content: string) {
    this._notifications.warn('Warning', content, this.form.getRawValue());
  }

  alert(content: string) {
    this._notifications.alert('Alert', content, this.form.getRawValue());
  }

}
