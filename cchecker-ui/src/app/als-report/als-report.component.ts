import { Component, OnInit, ViewChild, AfterViewInit, OnDestroy,ElementRef  } from '@angular/core';
import { ReportService } from '../services/report.service';
import { NgbTabset } from '../../../node_modules/@ng-bootstrap/ng-bootstrap';
import { RestService } from '../services/rest.service';
import { HttpClient } from '../../../node_modules/@angular/common/http';
import { saveAs }  from 'file-saver'
import { Subscription } from '../../../node_modules/rxjs';
@Component({
  selector: 'app-als-report',
  templateUrl: './als-report.component.html',
  styleUrls: ['./als-report.component.css']
})
export class AlsReportComponent implements OnInit, AfterViewInit, OnDestroy {
  dtFormOptions:Object;
  dtFormSummaryOptions:Object;
  dtSummaryOptions:Object;
  dtNrdsOptions:Object;
  dtCrfOptions:Object;
  error:boolean;
  errorMessage:String;  
  raveForm:Object;
  reportData:Object;
  showFormTab:Boolean=false;
  statusMessage:String;
  tabName:String;
  tabChanges:Subscription;

  @ViewChild(NgbTabset)
    tabs: NgbTabset;

  constructor(private reportService:ReportService, private restService:RestService,private http:HttpClient) { }

  // switch tab when selecting form from dropdown //
  changeForm = (raveForm, form) => {
    this.tabName = raveForm.formName;
    if (form) { // used to select dropdown via link //
      form.controls['raveForm'].setValue(raveForm)
    }
    if (this.showFormTab) {
      this.tabs.select("raveForm")
    }
    else {
      this.showFormTab = true;
    }
    this.raveForm = raveForm;
    return false;
  };

  // generates excel file after button is clicked //
  generateExcel = () => {
    this.statusMessage = null;
    this.error = false;
    this.restService.generateExcel().subscribe(
      data => {
        const filename = data.headers.get('Content-Disposition').replace('attachment; filename=','')
        var blob = new Blob([data.body], {type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"});
        saveAs(blob, filename);
        this.statusMessage = `${filename} downloaded succesfully.`
      }, 
      error => {
        this.error = true;
        const reader: FileReader = new FileReader();  
        reader.readAsText(error.error)
        reader.onloadend = (error):void => this.errorMessage = reader.result;
      },
      () => console.log("FINISHED"))
  };

  ngOnInit() { 
    // get reportData //
    this.reportService.getReportData().subscribe(data=> this.reportData = data).unsubscribe();

    // all options for data tables //
    const baseDtOptions = { ordering:false, paging:false, searching:false, info:false } // base datatable options //

    // options for standard crf module tables //
    this.dtCrfOptions = Object.assign({
      columns:[
        {width:"125px", cellType:"th", title:"CDE IDVersion"},
        {width:"225px", cellType:"th", title:"CDE Name"},
        {width:"225px", cellType:"th",title:"Template Name"},
        {width:"225px", cellType:"th", title:"CRF ID Version"}
      ],scrollY:400, scroller:true, scrollX:true
    }, baseDtOptions);

    // options for rave form page //
    this.dtFormOptions={
      columns: [{width:"70px"},{width:"80px"},{width:"80px"},{width:"80px"},{width:"170px"},{width:"300px"},{width:"200px"},{width:"200px"},{width:"250px"},{width:"120px"},{width:"170px"},{width:"150px"},{width:"150px"},{width:"150px"},{width:"150px"},{width:"150px"},{width:"120px"},{width:"260px"},{width:"120px"},{width:"160px"},{width:"120px"},{width:"120px"},{width:"120px"},{width:"120px"},{width:"120px"},{width:"150px"},{width:"150px"},{width:"120px"},{width:"150px"},{width:"120px"}],
      ordering:false,
      paging:false,
      searching:false,
      info:false,
      scrollX:true,
      scrollY:400,
      scroller:true
    };    

    // options for NRDS tables //
    this.dtNrdsOptions = Object.assign({columns:[
      {width:"15%",cellType:"th", title:"Rave Form OID"},
      {width:"15%",cellType:"th", title:"RAVE Field Order"},
      {width:"15%",cellType:"th", title:"RAVE Field Label"},
      {width:"15%",cellType:"th", title:"CDE ID Version"},
      {width:"15%",cellType:"th", title:"CDE Name"},
      {width:"15%",cellType:"th", title:"Result"},
      {width:"10%",cellType:"th", title:"Message"}
    ],
      scrollY:400,
      scroller:true
    },baseDtOptions);
        
    // options for summary bottom table //        
    this.dtFormSummaryOptions = Object.assign({columns:[{width:"25%",cellType:"th"},{width:"75%",cellType:"th"}]},baseDtOptions)
  
    // options for summary top table //
    this.dtSummaryOptions= Object.assign({columns:[{width:"50%",cellType:"th"},{width:"50%",cellType:"th"}] },baseDtOptions) //
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
    if (this.tabs) {
      this.tabChanges.unsubscribe();
    }
  }
}


