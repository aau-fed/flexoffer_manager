import { Component, Input } from '@angular/core';

@Component({
  selector: 'list-errors',
  templateUrl: './list-errors.component.html'
})
export class ListErrorsComponent {
  formattedErrors: Array<string> = [];

  @Input()
  set errors(errorList: Array<string>) {
    this.formattedErrors = [];

    if (errorList.length > 0) {
      for (const field in errorList) {
        this.formattedErrors.push(`${errorList[field]}`);
      }
    }
  };

  get errorList() { return this.formattedErrors; }


}
