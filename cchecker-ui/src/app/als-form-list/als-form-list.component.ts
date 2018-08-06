import { Component, OnInit } from '@angular/core';
import { validateConfig } from '@angular/router/src/config';
import { RestService } from '../services/rest.service';
import { HttpClient } from '@angular/common/http';
import { DataService } from '../services/data.service';
import { HttpEventType }  from '@angular/common/http';

@Component({
  selector: 'app-als-form-list',
  templateUrl: './als-form-list.component.html',
  styleUrls: ['./als-form-list.component.css']
})
export class AlsFormListComponent implements OnInit {
  checkedItems:String[];  // list of checked names //
  checkStdCrfCde:Boolean; // form checkbox at bottom of page //
  checkUom:Boolean; // form checkbox at bottom of page //
  formListData:Object; // main form list object to populate table //
  mustDisplayException:Boolean; // form checkbox at bottom of page //
  selectAllCheckbox:Boolean; // checkbox to select/de-select all items in form list table //
  validItemsLength:Number; // length of only success parsing status items //

  constructor(private http:HttpClient, private restService:RestService, private dataService:DataService) {
    this.selectAllCheckbox = true; // select all forms checkbox. default to all //
    this.checkedItems = []; // array of form names that are selected //
    this.formListData = {'formsList':[],'checkUom':null,'checkStdCrfCde':null,'mustDisplayException':null}; // data for form list table //
    this.getFormListData(); // get form list data from dataService //
    this.checkedItems = this.setCheckedItemsArray(); // simple array for checked forms //
    this.getFormListOptionCheckboxes();  // get 3 checkboxes at bottom and their values //   
   }

  ngOnInit() {
    this.validItemsLength = this.checkedItems.length;
    this.formListData['formsList'].sort((a, b) => a.formName.toUpperCase()<b.formName.toUpperCase() ? -1 : 1);    
  }

  // get checkbox status of record //
  getCheckedStatus = record => this.checkedItems.indexOf(record.formName) > -1;

  // gets form list data and sets checkedItems array //
  getFormListData() {
    this.formListData = this.dataService.getFormListData(); 
    this.checkedItems = this.setCheckedItemsArray();
  };

  // get 3 checkboxes and their values. Called on ngOnInit() //
  getFormListOptionCheckboxes() {
    const checkboxes = this.dataService.getFormListOptionCheckbox(['checkUom','checkStdCrfCde','mustDisplayException']);
    for (let c in checkboxes) { 
      this[c]=checkboxes[c];
    };
  };

  // get usable status message to display in table status column //
  getReadableErrorStatus = e => e ? 'Success':'Fail';

  // sets checked all items to all or none //
  setCheckAllStatus = e => {
    
    e.target.checked ? this.checkedItems = this.setCheckedItemsArray() : this.checkedItems = [];
    this.selectAllCheckbox = e.target.checked;
  }


  // add or remove record from checkedItems array //
  setCheckedItem = e => { 
    this.checkedItems.indexOf(e['formName'])==-1 ? 
    this.checkedItems.push(e['formName']):this.checkedItems.splice(this.checkedItems.indexOf(e['formName']),1);
    this.validItemsLength==this.checkedItems.length ? this.selectAllCheckbox = true : this.selectAllCheckbox = false; 
    return false;
  };

  // filter function to assign ALL valid records to checkedItems array //
  setCheckedItemsArray = () => Object.assign([],this.formListData['formsList'].filter((r) => r.isValid ).map((e) => e.formName)); 

  // shows or hides error row //
  setExpandCollapse = (e:boolean) => { e['expand']=!e['expand']; return false };

  // call data service and set formList checkbox so page can be refreshed //
  setFormListOptionCheckbox(event) {
    this.dataService.setFormListOptionCheckbox(event.target.name, event.target.checked)
  }
};
