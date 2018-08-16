import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'reportFilter'
})
export class ReportFilterPipe implements PipeTransform {
  // returns filtered standard CRF data depending on arg2 //
  transform(value: any, arg1:any, arg2:any): any {
    return value.filter(val=> val[arg1]==arg2);

  }

}
