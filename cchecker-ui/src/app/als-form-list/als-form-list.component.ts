import { Component, OnInit, OnDestroy } from '@angular/core';
import { RestService } from '../services/rest.service';
import { FormListService } from '../services/formlist.service';
import { Observable } from 'rxjs';
import { ReportService } from '../services/report.service'
import { Router } from '../../../node_modules/@angular/router';
import { HttpEventType }  from '@angular/common/http';

@Component({
  selector: 'app-als-form-list',
  templateUrl: './als-form-list.component.html',
  styleUrls: ['./als-form-list.component.css'],
  })
export class AlsFormListComponent implements OnInit {
  checkedItems:Observable<String[]>;
  errorMessage:String;  
  formValidationStatus:Number=1;
  formListData:Observable<Object>;
  validating:Boolean;
  validItemsLength:Observable<Object>;
  checkFormsService;
  feedService;

  constructor(private formListService:FormListService, private restService:RestService, private reportService:ReportService, private router:Router) {
  }

  ngOnInit() {
    this.checkedItems = this.formListService.getCheckedItems(); // get checked items as observable //
    this.formListData = this.formListService.getFormListData(); // get form data as observable //
    this.validating = false;
    this.validItemsLength = Object.assign([],this.formListData.source['value']['formsList'].filter((r) => r.isValid ).map((e) => e.formName)).length; // get valid item value //
  };

  // check forms (validate) and go to report page //
  checkForms() {
    let checkedItems:String[];
    this.errorMessage = null;
    let formListData:Object;
    this.checkedItems.subscribe(data=>checkedItems=data).unsubscribe();
    this.formListData.subscribe(data=>formListData=data).unsubscribe();
    this.validating = true;

    // run check forms //
    this.checkFormsService = 
      this.restService.checkForms(checkedItems,formListData).subscribe(
        data => {
          this.reportService.setReportData(data)
        },
        error => {
          this.errorMessage = error;
          this.validating = false;
        },
        () => {
          this.validating = false;
          this.router.navigateByUrl('/report')
        })

    // runs feed progress service //
    this.feedService = 
      this.restService.validateFeedStatus().subscribe(
        e => {
          if (e.type === HttpEventType.DownloadProgress) {
            let currentForm = e['partialText'].split('\n\n').filter(val => val!='' && val != 'data:').pop();
            if (currentForm) {
              this.formValidationStatus = currentForm.replace('data:','');
            }

          }
        },
        error => {
          console.log("ERROR")
        },
        () => {
          console.log("FINISHED")
        }
      )           
  };

  // gets current form for validation progress message //
  getCurrentForm = () => `${this.formValidationStatus}`;

  // gets checkd status of record //
  getCheckedStatus = record => this.formListService.getCheckedStatus(record);

  // gets checkd status of record //
  getParsingStatus = validFlag => validFlag ? 'Pass':'Fail';

  // sets all or none for checked status of records //
  setCheckAllStatus = (event):void => this.formListService.setCheckAllStatus(event.target.checked);

  // sets checked status of record
  setCheckedItem = (record):void => this.formListService.setCheckedItem(record);

  // set expand collapse property //
  setExpandCollapse = record => {
    this.formListService.setExpandCollapse(record);
    return false;
  };
  
  // sets checkUom, checkStdCrfCde and mustDisplayException checkboxes at bottom of page //
  setFormListOptionCheckbox = (event):void => this.formListService.setFormListOptionCheckbox(event);

  ngOnDestroy() {
  }
};
