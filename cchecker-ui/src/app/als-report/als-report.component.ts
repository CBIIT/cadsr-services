import { Component, OnInit, ViewChild, AfterViewInit, OnDestroy,ElementRef  } from '@angular/core';
import { ReportService } from '../services/report.service';
import { NgbTabset } from '../../../node_modules/@ng-bootstrap/ng-bootstrap';
import { Observable } from 'rxjs';
import { RestService } from '../services/rest.service';
import { HttpClient } from '../../../node_modules/@angular/common/http';
import { saveAs }  from 'file-saver'
import { Subscription } from '../../../node_modules/rxjs';
import { FormListService } from '../services/formlist.service';
@Component({
  selector: 'app-als-report',
  templateUrl: './als-report.component.html',
  styleUrls: ['./als-report.component.css']
})
export class AlsReportComponent implements OnInit, AfterViewInit, OnDestroy {
  dtCrfOptions:Object;
  dtFormOptions:Object;
  dtFormSummaryOptions:Object;
  dtNrdsOptions:Object;
  dtNrdsOptionsMissing:Object;
  dtSummaryOptions:Object;
  errorMessage:String;  
  formListData:Observable<Object>;
  raveForm:Object;
  reportData:Object;
  showFormTab:Boolean=false;
  statusMessage:String;
  tabChanges:Subscription;
  tabName:String;
  isGenerating:Boolean;
  loaded:Boolean;

  @ViewChild(NgbTabset)
    tabs: NgbTabset;

  constructor(private reportService:ReportService, private restService:RestService,private http:HttpClient,private formListService:FormListService) { 
  }

  // switch tab when selecting form from dropdown //
  changeForm = (raveForm, form) => {
    this.tabName = raveForm.formName;
    if (form) { form.controls['raveForm'].setValue(raveForm) };
    if (this.showFormTab) {
      this.tabs.select("raveForm")
    }
    else {
      this.showFormTab = true;
    };
    this.raveForm = raveForm;
    return false;
  };

  // generates excel file after button is clicked //
  generateExcel = () => {
    this.isGenerating = true;
    this.errorMessage = null;
    this.statusMessage = null;
    var that = this;
    this.restService.generateExcel(this.formListData.source['value'].sessionid).subscribe(
      data => {
        const filename = data.headers.get('Content-Disposition').replace('attachment; filename=','')
        var blob = new Blob([data.body], {type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"});
        saveAs(blob, filename);
        this.statusMessage = `${filename} downloaded succesfully.`
      }, 
      error => {
        this.isGenerating = false;
        this.errorMessage = 'Unexpected error, please contact Application Support (<a href="mailto:NCIAppSupport@nih.gov">NCIAppSupport@nih.gov</a>)';
        const reader: FileReader = new FileReader();  
        reader.readAsText(error.error)
        reader.onloadend = (error):void => this.errorMessage = reader.result;
      },
      () => {
        that.isGenerating = false;
      });

      

  };

  ngOnInit() { 
    // get reportData //
    this.reportData = {'cccForms':[]};
    this.restService.getReportFromLocation(this.reportService.getReportLocation()).subscribe(
      data => {
        this.reportData = data;
        this.loaded = true;
      },
      error => {
        this.loaded = true;
      },
      () => {
        this.loaded = true;
      }
    );
    this.formListData = this.formListService.getFormListData(); // get form data as observable //


    // all options for data tables //
    const baseDtOptions = { ordering:false, paging:false, searching:false, info:false } // base datatable options //

    // options for standard crf module tables //
    this.dtCrfOptions = Object.assign({
      columns:[
        {width:"125px", cellType:"th", title:"CDE PID"},
        {width:"225px", cellType:"th", title:"CDE Long Name"},
        {width:"225px", cellType:"th", title:"Preferred Question Text"},
        {width:"150px", cellType:"th", title:"Type"},        
        {width:"225px", cellType:"th",title:"Template Name"},
        {width:"125px", cellType:"th", title:"CRF PID"}
      ],
      scrollY:350,
      scroller:true,
      scrollX:true
      }, baseDtOptions
    );

    // options for rave form page //
    this.dtFormOptions= Object.assign({
      columns: [{width:"70px"},{width:"70px"},{width:"160px"},{width:"80px"},{width:"170px"},{width:"300px"},{width:"200px"},{width:"200px"},{width:"250px"},{width:"120px"},{width:"170px"},{width:"150px"},{width:"150px"},{width:"150px"},{width:"300px"},{width:"150px"},{width:"120px"},{width:"260px"},{width:"120px"},{width:"160px"},{width:"120px"},{width:"120px"},{width:"120px"},{width:"120px"},{width:"120px"},{width:"150px"},{width:"150px"},{width:"120px"},{width:"150px"},{width:"120px"}],
      scrollX:true,
      scrollY:300,
      scroller:true
      }, baseDtOptions
    );    

    // options for NRDS tables //
    this.dtNrdsOptions = Object.assign({
      columns:[
        {width:"13%",cellType:"th", title:"Rave Form OID"},
        {width:"13%",cellType:"th", title:"RAVE Field Order"},
        {width:"13%",cellType:"th", title:"RAVE Field Label"},
        {width:"13%",cellType:"th", title:"CDE PID"},
        {width:"13%",cellType:"th", title:"CDE Long Name"},
        {width:"13%",cellType:"th", title:"Result"},
        {width:"10%",cellType:"th", title:"Message"},
        {width:"12%",cellType:"th", title:"Type"}
      ],
      scrollY:350,
      scroller:true
      }, baseDtOptions
    );

    // options for NRDS tables //
    this.dtNrdsOptionsMissing = Object.assign({
      columns:[
        {width:"35%",cellType:"th", title:"CDE PID"},
        {width:"25%",cellType:"th", title:"CDE Long Name"},
        {width:"40%",cellType:"th", title:"Preferred Question Text"}
      ],
      scrollY:350,
      scroller:true
      }, baseDtOptions
    );    
        
    // options for summary bottom table //        
    this.dtFormSummaryOptions = Object.assign({
      columns:[
        {width:"25%",cellType:"th"},
        {width:"75%",cellType:"th"}
      ]}, baseDtOptions
    )
  
    // options for summary top table //
    this.dtSummaryOptions= Object.assign({
      columns:[
        {width:"50%",cellType:"th"},
        {width:"50%",cellType:"th"}]
      }, baseDtOptions) //
    };

  ngAfterViewInit() {
    // after view is created subscribe to tab changes. Only done for css to create border on non active tab since ngb tabsets do not support classes //
    if (this.tabs) {
      this.tabChanges = this.tabs.tabs.changes.subscribe(data=>{
        this.tabs.select("raveForm")
      });
    };
  }

  ngOnDestroy() {
    // unsubscribe from tab changes //
    if (this.tabs) {
      this.tabChanges.unsubscribe();
    }
  }
}


