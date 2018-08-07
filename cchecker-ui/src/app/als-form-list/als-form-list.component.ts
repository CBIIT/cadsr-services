import { Component, OnInit } from '@angular/core';
import { RestService } from '../services/rest.service';
import { DataService } from '../services/data.service';
import { Observable } from '../../../node_modules/rxjs';

@Component({
  selector: 'app-als-form-list',
  templateUrl: './als-form-list.component.html',
  styleUrls: ['./als-form-list.component.css'],
  })
export class AlsFormListComponent implements OnInit {

  private formListData:Observable<Object>;
  private checkedItems:Observable<String[]>;
  private validItemsLength:Number;

  constructor(private dataService:DataService) {
  }

  ngOnInit() {
    this.formListData = this.dataService.getFormListData(); // get form data as observable //
    this.checkedItems = this.dataService.getCheckedItems(); // get checkd items as observable //
    this.validItemsLength = Object.assign([],this.formListData.source['value']['formsList'].filter((r) => r.isValid ).map((e) => e.formName)).length; // get valid item value //
  };

  // gets checkd status of record //
  getCheckedStatus(record) {
     return this.dataService.getCheckedStatus(record);
  };

  // sets checked status of record
  setCheckedItem = (record):void => {
    this.dataService.setCheckedItem(record);
  };

  // sets all or none for checked status of records //
  setCheckAllStatus(event):void {
    this.dataService.setCheckAllStatus(event.target.checked);
  };

  // set expand collapse property //
  setExpandCollapse(record) {
    this.dataService.setExpandCollapse(record);
    return false;
  };
  
  // sets checkUom, checkStdCrfCde and mustDisplayException checkboxes at bottom of page //
  setFormListOptionCheckbox(event):void {
    this.dataService.setFormListOptionCheckbox(event);
  };


};
