import { Component, Input } from '@angular/core';
import { DialogMeta } from './models/dialog-meta.model';
@Component({
  selector: 'modalbox',
  templateUrl: './modalbox.component.html',
  styleUrls: []
})
export class ModalboxComponent{


  constructor() { }

  title: String;
  body: String;



  @Input()
  set meta(d: DialogMeta) {

    this.title = d.TITLE;
    this.body = d.CONTENT;

    
  };


}
