import { FMANApiService } from '../shared/services/fmanapi.service';
import { Component, OnInit, OnDestroy, AfterViewInit, ViewChild, ElementRef, HostListener, ViewContainerRef } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';


import { UserService } from '../shared/services';
import { DialogMeta } from '../shared/models/dialog-meta.model';
import { PageChangedEvent } from 'ngx-bootstrap/pagination';
import { FlexofferModalService } from '../shared/helpers/flexoffermodal.service';
import { AlertService } from '../shared/services/alert.service';
import { UserlistService } from '../userlist/userlist.service';
import { moment } from 'ngx-bootstrap/chronos/test/chain';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

declare var $: any;


@Component({
  selector: 'app-user-contract',
  templateUrl: './user-contract.component.html'
})
export class UserContractComponent implements OnInit {
  loggedIn = false;
  userName = '';
  contractForm: FormGroup;
  contract: any = null;


  @ViewChild('chartContainer')
  private chartContainer: ElementRef;

  constructor(
    private router: Router,
    private fb: FormBuilder,
    private userService: UserService,
    private userlistService: UserlistService,
    private fmanApiService: FMANApiService,
    private foModalService: FlexofferModalService,
    private alertService: AlertService,
    private route: ActivatedRoute
  ) {

    this.contractForm = this.fb.group({
      userId: ['', Validators.required],
      energyFlexReward: ['0', [Validators.required, Validators.min(0)]],
      fixedReward: ['0', [Validators.required, Validators.min(0)]],
      schedulingEnergyReward: ['0', [Validators.required, Validators.min(0)]],
      schedulingFixedReward: ['0', [Validators.required, Validators.min(0)]],
      schedulingStartTimeReward: ['0', [Validators.required, Validators.min(0)]],
      timeFlexReward: ['0', [Validators.required, Validators.min(0)]],
      imbalanceLimitPerTimeInterval: ['0', [Validators.required, Validators.min(0)]]
    });
  }

  ngOnInit() {
    if (!this.userService.isLoggedIn()) {
      this.router.navigateByUrl('/login');
    }

    this.userName = (this.route.snapshot.paramMap.get('userName') || '');

    if (this.userName === '') {
       this.userService.currentUser.subscribe(userName => {
         this.userName = userName.userName;
         this.getContract();
       })
    } else {
      this.getContract();
    }

  };

  get f() { return this.contractForm.controls; }

  getContract() {
    this.userlistService
    .getContract(this.userName)
    .subscribe(data => {
      this.contractForm.patchValue(data.data);
    });
  }

  contractSubmit() {
    let val = this.contractForm.value;
    this.userlistService
        .saveContract(this.userName, val)
        .subscribe((data) => {
          this.contractForm.patchValue(data);
          this.contractForm.markAsPristine();
          this.alertService.info('Contract details have been successfully submitted.');
         }, (error) => {
          this.alertService.error('Contract details failed to be submitted.');
        });
  }


}



