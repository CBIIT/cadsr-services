import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})

export class RestService {
  constructor(private http:HttpClient) {
  }

  // generate excel report //
  generateExcel = () => this.http.get('http://172.16.239.11/gateway/genexcelreporterror',
  {
    observe:'response',
    responseType: "blob",
    withCredentials:true
  }
  );

  // validation service //
  checkForms(checkedItems,formListData){
    const checkUom = formListData['checkUom'] ? 'true':'false';
    const checkCRF = formListData['checkStdCrfCde'] ? 'true':'false';
    const displayExceptions = formListData['mustDisplayException'] ? 'true':'false';
    return this.http.post('http://172.16.239.11/gateway/checkservice',checkedItems,
    {
      withCredentials:true
    })
  } ; 

  // upload file service //
  uploadAlsFile(file, name){
    return this.http.post(`http://172.16.239.11/gateway/parseservice?owner=${name}`,file,
    {
      observe:"events",
      reportProgress:true,
      withCredentials:true,
    })} ;

  // gets validation status //
  validateFeedStatus() {
    return this.http.get('http://172.16.239.11/gateway/feedvalidatestatus',
    {
      observe:'events',
      reportProgress:true,
      responseType: "text",
      withCredentials:true
    });
  }




}
