import { Component, OnInit, OnDestroy } from '@angular/core';
import { RestService } from '../services/rest.service';
import { FormListService } from '../services/formlist.service';
import { Observable } from 'rxjs';
import { ReportService } from '../services/report.service'
import { Router, ChildActivationStart } from '../../../node_modules/@angular/router';
import { HttpEventType }  from '@angular/common/http';
import { NgControlStatus } from '@angular/forms';
import { looseIdentical } from '@angular/core/src/util';

@Component({
  selector: 'app-als-form-list',
  templateUrl: './als-form-list.component.html',
  styleUrls: ['./als-form-list.component.css'],
  })
export class AlsFormListComponent implements OnInit {
  checkedItems:Observable<String[]>;
  errorMessage:String;  
  errorMessage2:String;  
  formValidationStatus:Object={currFormName: "", currFormNumber: 1, countValidatedQuestions: 0};
  formListData:Observable<Object>;
  totalQuestionCount:Number;
  fileName:String;
  userName:String;
  validating:Boolean;  
  validItemsLength:Observable<Object>;
  checkFormsService;
  feedService;
  cancelButtonStatus:Boolean;
  currentRoute:String;
  isGeneratingXML:Boolean;

  constructor(private formListService:FormListService, private restService:RestService, private reportService:ReportService, private router:Router) {
  }

  ngOnInit() {
    this.currentRoute = this.router.url.replace('/', '');
    this.isGeneratingXML = this.formListService.getSessionDataItem('isGeneratingXML');
    this.cancelButtonStatus = this.formListService.getCancelButtonStatus();
    this.checkedItems = this.formListService.getCheckedItems(); // get checked items as observable //
    this.formListData = this.formListService.getFormListData(); // get form data as observable //
    this.fileName = this.formListService.getFileName(); // get filename data as string //
    this.userName = this.formListService.getUserName(); // get username data as string //
    this.validating = false;
    this.validItemsLength = Object.assign([],this.formListData.source['value']['formsList'].filter((r) => r.isValid ).map((e) => e.formName)).length; // get valid item value //
    if (this.formListService.getValidationStatus()) {
      this.validating = true;
      this.getFeedService();
    };

    this.totalQuestionCount=this.getTotalQuestionCount();

  };

  // returns page title based on route //
  getTitle = () => {
    if (this.currentRoute=='forms') {
      return 'Form List'
    }
    else {
      return 'Generate XML'
    }
  }

  isFormListPage = () => {
    if (this.currentRoute == 'forms') {
      return 1;
    }
    else {
      return 0;
    }    
  }

  cancelValidation() {
    this.formListService.setCancelButtonStatus(true);
    this.cancelButtonStatus = true;
    this.restService.cancelValidation(this.formListData.source['value'].sessionid).subscribe(
      data => {
        console.log(data);
      },
      error => {
        console.log(error)
      },
      () => {
        console.log("DONE")
      }
    )
    }
  // check forms (validate) and go to report page //
  checkForms() {
    this.formListService.setCancelButtonStatus(false);
    let checkedItems:String[];
    this.errorMessage = null;
    this.errorMessage2 = null;
    let formListData:Object;
    this.totalQuestionCount=this.getTotalQuestionCount();
    this.checkedItems.subscribe(data=>checkedItems=data).unsubscribe();
    this.formListData.subscribe(data=>formListData=data).unsubscribe();
    this.validating = true;

    // run check forms //
    this.checkFormsService = 
      this.formListService.setValidationStatus(true);
      this.restService.checkForms(checkedItems,formListData,this.formListData.source['value'].sessionid).subscribe(
        data => {
          this.reportService.setReportLocation(data);
        },
        error => {
          this.formListService.setValidationStatus(false);

          this.errorMessage = 'Cannot communicate with the server. <br /><br />If this continues to fail report to <a href="/contact-us">NCI Application Support</a>.'
          if (error.error=='' || typeof(error.error)=='object') {
            this.errorMessage = 'Cannot communicate with the server. <br /><br />If this continues to fail report to <a href="/contact-us">NCI Application Support</a>.'
          }
          else {
            this.errorMessage = error.error + '<br /><br />If this continues to fail report to <a href="/contact-us">NCI Application Support</a>.';
          }

          this.validating = false;
        },
        () => {
          this.formListService.setCancelButtonStatus(false);
          this.validating = false;
          this.formListService.setValidationStatus(false);
          this.router.navigateByUrl('/report')
        })

        this.getFeedService();
  };

  // runs feed progress service //
  getFeedService = () => {
  this.feedService = 
    this.restService.validateFeedStatus(this.formListData.source['value'].sessionid).subscribe(
      e => {
        if (e.type === HttpEventType.DownloadProgress) {
          let currentForm = e['partialText'].split('\n\n').filter(val => val!='' && val != 'data:').pop();
          if (currentForm) {
            this.formValidationStatus = JSON.parse(currentForm.replace('data:',''));
          }

        }
      },
      error => {
          this.validating = false;
          this.formListService.setValidationStatus(false);
      },
      () => {
          this.validating = false;
          this.formListService.setValidationStatus(false);
      }
    )
  };

  // gets current form for validation progress message //
  getCurrentForm = () => `${this.formValidationStatus['currFormNumber']}`;

  // gets current form for validation progress message //
  getCurrentFormName = () => `${this.formValidationStatus['currFormName']}`; 

  // gets current question count that has been validated //
  getCurrentQuestionCount = () => {
    if (this.formValidationStatus['currFormName']!='') {
      return ' - [Questions ' + this.formValidationStatus['countValidatedQuestions'] + '/' + this.totalQuestionCount + '] (' + this.formValidationStatus['currFormName'] + ')';
    }
    else {
      return '';
    }
  }

  // gets total question count of selected items //
  getTotalQuestionCount = () => {
    let qc = 0;
    let ci = JSON.parse(sessionStorage.getItem('checkedItems'));
    if (ci) {
      for (var x=0; x<ci.length; x++) {
        this.formListData.source['value']['formsList'].filter(function(item) {
          if (item['formName']==ci[x]) {
            qc+=item['questionsCount']
          }
        });
      }
    }

    return qc
  }

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
    if (this.feedService) { 
      this.feedService.unsubscribe();
    };
  }

  
};