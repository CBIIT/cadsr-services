import { Component, OnInit } from '@angular/core';
import { RestService } from '../services/rest.service';
import { FormListService } from '../services/formlist.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-als-form-list',
  templateUrl: './als-form-list.component.html',
  styleUrls: ['./als-form-list.component.css'],
  })
export class AlsFormListComponent implements OnInit {

  private formListData:Observable<Object>;
  private checkedItems:Observable<String[]>;
  private validItemsLength:Number;
  private validating:Boolean;
  constructor(private formListService:FormListService, private restService:RestService) {
  }

  ngOnInit() {
    this.formListData = this.formListService.getFormListData(); // get form data as observable //
    this.checkedItems = this.formListService.getCheckedItems(); // get checkd items as observable //
    this.validItemsLength = Object.assign([],this.formListData.source['value']['formsList'].filter((r) => r.isValid ).map((e) => e.formName)).length; // get valid item value //
    this.validating = false;
  };

  // check forms (validate) and go to report page //
  checkForms() {
    this.validating = true;
    let checkedItems:String[];
    let formListData:Object;
    this.checkedItems.subscribe(data=>checkedItems=data);
    this.formListData.subscribe(data=>formListData=data);
    this.restService.checkForms(checkedItems,formListData).subscribe(
      data => console.log(data),
      error => console.log(error),
      () => this.validating = false)
  }

  // gets checkd status of record //
  getCheckedStatus = record => this.formListService.getCheckedStatus(record);

  // sets checked status of record
  setCheckedItem = (record):void => this.formListService.setCheckedItem(record);

  // sets all or none for checked status of records //
  setCheckAllStatus = (event):void => this.formListService.setCheckAllStatus(event.target.checked);

  // set expand collapse property //
  setExpandCollapse = record => {
    this.formListService.setExpandCollapse(record);
    return false;
  };
  
  // sets checkUom, checkStdCrfCde and mustDisplayException checkboxes at bottom of page //
  setFormListOptionCheckbox = (event):void => this.formListService.setFormListOptionCheckbox(event);


};
