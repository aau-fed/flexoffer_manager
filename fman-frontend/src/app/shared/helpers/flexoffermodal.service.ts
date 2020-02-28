import { Component, ViewContainerRef, Injectable, ComponentRef, Input, OnInit } from '@angular/core';
import * as d3 from 'd3';
import { BsModalService, BsModalRef } from 'ngx-bootstrap/modal';

@Injectable()
export class FlexofferModalService {

    private config = {
      animated: true,
      keyboard: true,
      backdrop: true,
      ignoreBackdropClick: true,
      class: 'big-model-dialog'
    };

    constructor(private modalService: BsModalService) {}

    public showFoVisualization(flexOffer: any) {
        const modalRef = this.modalService.show(FOModalViewFO, this.config);
        modalRef.content.title = 'FlexOffer Overview';
        modalRef.content.closeBtnName = 'Close';
        modalRef.content.flexoffer = flexOffer;
        modalRef.content.listing = JSON.stringify(flexOffer, null, '\t');
    }
}

@Component({
  selector: 'modal-content-fo',
  template: `
    <div class="modal-header">
      <h4 class="modal-title pull-left">{{title}}</h4>
      <button type="button" class="close pull-right" aria-label="Close" (click)="bsModalRef.hide()">
        <span aria-hidden="true">&times;</span>
      </button>
      
      <div style="float: right;">
      View As: <select [(ngModel)]="viewMode">
          <option value="0">Graph</option>
          <option value="1">Cost functions</option>
          <option value="2">JSON</option>
               </select>
      </div>
    </div>    
    <div class="modal-body">
     <div>
      <flexofferview *ngIf="viewMode == 0" [flexoffer]="flexoffer" [maxWidth]="850">
      </flexofferview>

      <flexoffercostview *ngIf="viewMode == 1" [flexoffer]="flexoffer" [maxWidth]="850">
      </flexoffercostview>

      <pre *ngIf="viewMode == 2">
       {{listing}}
      </pre>
     </div>
    </div>
    <div class="modal-footer">
      <button type="button" class="btn btn-default" (click)="bsModalRef.hide()">{{closeBtnName}}</button>
    </div>
  `
})
export class FOModalViewFO implements OnInit {
  title: string;
  closeBtnName: string;
  viewMode: number = 0;
  flexoffer: any = null;
  listing: string = "";

  constructor(public bsModalRef: BsModalRef) {}

  ngOnInit() { }
}


