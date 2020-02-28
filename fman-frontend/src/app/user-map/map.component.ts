import { Component, OnInit, AfterViewChecked, TemplateRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { User } from '../shared';

import { CommonService } from '../shared/services/common.service';

import { UserService } from '../shared/services';
import { DialogMeta } from '../shared/models/dialog-meta.model';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { BsModalService } from 'ngx-bootstrap/modal';
import { UserlistService } from '../userlist/userlist.service';
import { FormGroup, FormBuilder } from '@angular/forms';
import { MouseEvent } from '@agm/core';

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.css']
})
export class MapComponent implements OnInit {

  loader = false;
  meta: DialogMeta = new DialogMeta();
  dataTableContext = null;
  public users: any[] = [];

  mapCenterLat: number;
  mapCenterLng: number;
  zoom: number = 4;

  modalRef: BsModalRef;
  contractForm: FormGroup;
  bill: any = {};

  constructor(
    private route: ActivatedRoute,
    private cService: CommonService,
    private router: Router,
    private userService: UserService,
    private modalService: BsModalService,
    private userlistService: UserlistService,
    private fb: FormBuilder,
    private _router: Router
  ) {
    this.contractForm = this.fb.group({
      'energyFlexReward': [''],
      'fixedReward': [''],
      'schedulingEnergyReward': [''],
      'schedulingFixedReward': [''],
      'schedulingStartTimeReward': [''],
      'timeFlexReward': ['']
    });
  }


  ngOnInit() {
    this.userService
      .getMaps()
      .subscribe(data => {
        // console.log(data);
        // this.prosumers = (data);
        this.users = data;
        this.meta.TITLE = "User Map";
        this.mapCenterLat = this.users[0].location.latitude;
        this.mapCenterLng = this.users[0].location.longitude;
        // this.meta.CONTENT = "Content testing";

      });
  }

  mapClicked($event: MouseEvent) {
  }

  onMouseOver(infoWindow, gm) {

    if (gm.lastOpen != null) {
      gm.lastOpen.close();
    }

    gm.lastOpen = infoWindow;

    infoWindow.open();
  }

  
  openContract(username) {
    this._router.navigate(['/user/contract', username]);
  }

  openBill(username) {
    this._router.navigate(['/user/bill', username]);
  }
}
