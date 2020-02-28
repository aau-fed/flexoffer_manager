import { Component, OnInit, AfterViewChecked } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { User } from '../shared';
import { HeadquarterService } from './headquarter.service';
import { Headquarter } from './headquarter.model';

import { CommonService } from '../shared/services/common.service';
import { TranslateService }   from '../translate';

import { UserService } from '../shared/services';
import { DialogMeta } from '../shared/models/dialog-meta.model';

declare var $ :any;

@Component({
  selector: 'app-headquarter',
  templateUrl: './headquarter.component.html',
  styleUrls: ['./headquarter.component.css']
})
export class HeadquarterComponent implements OnInit//,AfterViewChecked    
{

  //pci_event: PciUser = new PciUser();
  isDeleting = false;
  isSubmitting = false;
  loader = false;
  meta: DialogMeta = new DialogMeta();
  
  constructor(
    private route: ActivatedRoute,
    private uService: HeadquarterService,
    private cService: CommonService,
    private router: Router,
    private translate: TranslateService,
    private userService: UserService
  ) {}

  dataTableContext = null;
  items;
  regions;
  countries;

  region_id;
  country_id;


  ngOnInit() {
    this.userService.currentUser.subscribe(
      (userData) => {
  
        // if(userData.authority_name != 'TMC_ADMIN')
        // {
        //   this.router.navigateByUrl('/error');
        // }
        
      }
    );
    

    this.items = this.route.snapshot.data['headquarters'].headquarters;

    this.meta.TITLE = "TITLE testing";
    this.meta.CONTENT = "Content testing";
    
  }


  ngAfterViewChecked(){

    // if(this.dataTableContext == null)
    // {
    //   this.dataTableContext = $('#dataTable').DataTable({
    //     'paging'      : true,
    //     'lengthChange': false,
    //     'searching'   : true,
    //     'ordering'    : true,
    //     'info'        : true,
    //     'autoWidth'   : false
    //   });
    // }
  }

  doFilter(){
    this.loader = true;
    this.isSubmitting = true;

    this.uService.getPaged(this.country_id).subscribe(
      (data) => {
        this.items = [];
        this.items = data['headquarters'];
        this.loader = false;
        this.isSubmitting = false;
      });
  }


  openEditPage(item){
    this.router.navigateByUrl('/headquarter/'+item.ID);
  }

  delete(item : Headquarter) {
    this.isDeleting = true;

    this.uService.destroy(item.ID)
      .subscribe(
        success => {
          $('#tr_item_'+item.ID).fadeOut('medium', function(){
              $(this).remove();
              
          })
          this.isDeleting = false;
        }
      );
      // this.isSubmitting = false;
  }


  newform()
  {
    this.router.navigateByUrl('/headquarter');
  }

  back()
  {
    this.router.navigateByUrl('/admin');
  }

  item : Headquarter = new Headquarter();
  editing_item : Headquarter = new Headquarter();
  dialog_title;
  dialog_body;
  dialog_mode = '';
  showDialog(o : Headquarter, mode) 
  {
    this.dialog_mode = mode;
    if(mode == 'CANCEL')
    {
      this.dialog_title = "dialog_title";
      this.dialog_body = "cancel_question";
    }
    else
    if(mode == 'DELETE')
    { 
      this.item = o;      
      this.dialog_title = "dialog_title";
      this.dialog_body = "delete_question";
    }
    else
    if(mode == 'UPDATE')
    { 
      o.HEADQUARTERS_NAME = $('#HEADQUARTERS_NAME_'+o.ID).html();
      this.item = o;      
      this.dialog_title = "dialog_title";
      this.dialog_body = "save_question";
    }

    $('#dialogbox').modal('show'); 
  }

  executeModalAction()
  {
    if(this.dialog_mode == 'UPDATE')
    {
      this.update();
    }
    else
    if(this.dialog_mode == 'DELETE')
    {
      this.delete(this.item);
    }
    else
    if(this.dialog_mode == 'CANCEL')
    {
      //console.log("cancel here....");
      this.restoreValues();
    }
  }

  restoreValues()
  {
    $('#HEADQUARTERS_NAME_'+this.editing_item.ID).html(this.editing_item.HEADQUARTERS_NAME);
    
  }

  update() {

    // console.log(this.item);

    this.uService
    .save(this.item)
    .subscribe();
  }

  onRowClick(event, o : Headquarter) {
    this.editing_item = o;
  }
}
