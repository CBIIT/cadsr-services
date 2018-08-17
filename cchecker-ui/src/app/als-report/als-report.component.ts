import { Component, OnInit, ViewChild, AfterViewInit, OnDestroy  } from '@angular/core';
import { ReportService } from '../services/report.service';
import { NgbTabset } from '../../../node_modules/@ng-bootstrap/ng-bootstrap';
import { RestService } from '../services/rest.service';
import { HttpClient } from '../../../node_modules/@angular/common/http';
import { saveAs }  from 'file-saver'
import { Observable, Subscription } from '../../../node_modules/rxjs';
@Component({
  selector: 'app-als-report',
  templateUrl: './als-report.component.html',
  styleUrls: ['./als-report.component.css']
})
export class AlsReportComponent implements OnInit, AfterViewInit, OnDestroy {
  congruentFormTotal:String;
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

  changeForm = (form) => {
    this.tabName = form.formName;
    if (this.showFormTab) {
      this.tabs.select("raveForm")
    }
    else {
      this.showFormTab = true;
    }
    this.raveForm = form;
    return false;
  }

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
    this.reportService.getReportData().subscribe(data=> this.reportData = data).unsubscribe();
    this.congruentFormTotal = this.reportData['cccForms'].filter(v => v.congruencyStatus=='CONGRUENT').length;
    const baseDtOptions = { ordering:false, paging:false, searching:false, info:false }
    this.dtSummaryOptions= Object.assign({columns:[{width:"50%",cellType:"th"},{width:"50%",cellType:"th"}] },baseDtOptions) //
    this.dtFormSummaryOptions = Object.assign({columns:[{width:"25%",cellType:"th"},{width:"75%",cellType:"th"}]},baseDtOptions)
    this.dtNrdsOptions = Object.assign({columns:[
      {width:"15%",cellType:"th", title:"Rave Form OID"},
      {width:"15%",cellType:"th", title:"RAVE Field Order"},
      {width:"15%",cellType:"th", title:"RAVE Field Label"},
      {width:"15%",cellType:"th", title:"CDE ID Version"},
      {width:"15%",cellType:"th", title:"CDE Name"},
      {width:"15%",cellType:"th", title:"Result"},
      {width:"10%",cellType:"th", title:"Message"}
    ]},baseDtOptions)
    this.dtCrfOptions = Object.assign({
      columns:[
        {width:"125px", cellType:"th", title:"CDE IDVersion"},
        {width:"225px", cellType:"th", title:"CDE Name"},
        {width:"225px", cellType:"th",title:"Template Name"},
        {width:"225px", cellType:"th", title:"CRF ID Version"}
      ],scrollY:400, scroller:true, scrollX:true
    }, baseDtOptions)
    this.dtFormOptions={
      columns: [{width:"70px"},{width:"80px"},{width:"80px"},{width:"80px"},{width:"150px"},{width:"300px"},{width:"300px"},{width:"200px"},{width:"700px"},{width:"120px"},{width:"120px"},{width:"120px"},{width:"400px"},{width:"700px"},{width:"400px"},{width:"700px"},{width:"120px"},{width:"500px"},{width:"120px"},{width:"120px"},{width:"120px"},{width:"120px"},{width:"120px"},{width:"120px"},{width:"120px"},{width:"120px"},{width:"120px"},{width:"120px"},{width:"120px"},{width:"120px"}],
      ordering:false,
      paging:false,
      searching:false,
      info:false,
      scrollX:true,
      scrollY:400,
      scroller:true
    };
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


