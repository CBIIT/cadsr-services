import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { environment } from './../../environments/environment';

@Injectable({
  providedIn: 'root'
})

export class RestService {
  REST_API:string;

  constructor(private http:HttpClient, private router:Router) {
    this.REST_API = environment.REST_API;
  }

  cancelValidation(sessionid) {
    return this.http.get(`${this.REST_API}/gateway/cancelvalidation/${sessionid}`,
    {
      withCredentials:true,
      observe:'response',
      responseType: "json",
    });
  };

  // validation service //
  checkForms(checkedItems,formListData, sessionid){
    const checkUom = formListData['checkUom'] ? 'true':'false';
    const checkCRF = formListData['checkStdCrfCde'] ? 'true':'false';
    const displayExceptions = formListData['mustDisplayException'] ? 'true':'false';
    // return this.http.post(`${this.REST_API}/gateway/checkservice?checkCRF=${checkCRF}`,checkedItems,
    return this.http.post(`${this.REST_API}/gateway/checkservice?checkCRF=${checkCRF}&sessionid=${sessionid}`,checkedItems,
    {
      withCredentials:true,
      responseType: 'text'

    })
  };

  // generate excel report //
  generateExcel = (sessionid) => this.http.get(`${this.REST_API}/gateway/genexcelcheckreport/${sessionid}`,
  {
    observe:'response',
    responseType: "blob",
    withCredentials:true
  }
  );

  // gets report data from location //
  getReportFromLocation = (location) => {
    return this.http.get(`${this.REST_API}/gateway/retrievereporterror/${location}`,
    {
      withCredentials:true
    });
  };

  // gets contexts for xml generation //
  getXmlContexts = () => {
    return this.http.get(`${this.REST_API}/gateway/retrievecontexts`,
      {
        withCredentials: true
      });
  };

  // upload file service //
  uploadAlsFile(file, name){
    return this.http.post(`${this.REST_API}/gateway/parseservice?owner=${name}`,file,
    {
      observe:"events",
      reportProgress:true,
      withCredentials:true,
    })} ;

  // gets validation status //
  validateFeedStatus(sessionid) {
    return this.http.get(`${this.REST_API}/gateway/feedvalidatestatus/${sessionid}`,
    {
      observe:'events',
      reportProgress:true,
      responseType: "text",
      withCredentials:true
    });
  }




}
