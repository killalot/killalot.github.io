---
layout: post
title: "Combining Jasper reports in a Spring controller"
date: 2014-07-23 20:37:51 +0100
comments: true
categories: Spring, Jasper
excerpt: I have come across a scenario where I was required to combine multiple instances of the same Jasper report into a single pdf file. So here is a Spring controller that demonstrates this. I have also included a method for printing a single report as well so you can see the difference.
---

<p>I have come across a scenario where I was required to combine multiple instances of the same Jasper report into a single pdf file. So here is a Spring controller that demonstrates this. I have also included a method for printing a single report as well so you can see the difference.</p>

``` java
@Controller
public class CombineReportsController {

    @Autowired
    private EntityDao entityDao;

    @RequestMapping(value="/print_selected",method=RequestMethod.GET)
    public void printSelected(@RequestParam("ids[]") Long[] ids,HttpServletResponse response) throws JRException, IOException{
        // get the jrxml file from the class path
        URL reportPath  = getClass().getResource("/jasper_reports/print_multiple.jrxml");
        List<JasperPrint> jpList = new ArrayList<>();
        // gather any entities that need to be passed to jasper
        List<Entity> entities = entityDao.printMultiple(ids);
        for(Entity entity : entities){            
            HashMap<String,Object> params = new HashMap<>();
            // add entity and any other parameters
            params.put("entity", entity);

            // create JasperReport for each entity instance
            JasperReport jreport = JasperCompileManager.compileReport(reportPath.getPath());
            JasperPrint jprint = JasperFillManager.fillReport(jreport, params, new JRBeanCollectionDataSource (entity.getItems()));            

            // add report to list
            jpList.add(jprint);     
        }
        // export reports using a single exporter and pass output stream of response object
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setParameter(JRPdfExporterParameter.JASPER_PRINT_LIST, jpList);
        exporter.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
        exporter.exportReport();
    }

    @RequestMapping(value="/{id}/print")
    public ModelAndView printSingle(ModelMap modelMap, HttpServletRequest request,@PathVariable("id") Long id){            

        Entity entity = entityDao.printSingle(id);
        JRDataSource datasource = new JRBeanCollectionDataSource (entity.getItems());
        modelMap.addAttribute("datasource", datasource);
        modelMap.addAttribute("entity", entity);
        modelMap.addAttribute("format", "pdf");        

        // "entityPrintSingle" is the bean id, declared inside the jasper-views.xml
        return new ModelAndView("entityPrintSingle", modelMap);
    }
}
```

<p>The main difference between the 2 methods is that in the printSingle() method, Spring exports the Jasper report itself using the returned ModelAndView object. However the printSelected() method is performing the export manually in order to provide the list of Jasper reports to the exporter.</p>

<h2>References</h2>
<ul>
	<li><a target="_blank" href="http://stackoverflow.com/questions/8564163/how-to-collate-multiple-jrxml-jasper-reports-into-a-one-single-pdf-output-file">Multiple jrxml reports into one file</a></li>
	<li><a target="_blank" href="http://krams915.blogspot.co.uk/2010/12/spring-3-mvc-dynamic-jasper-integration.html">Dynamic jasper integration</a></li>
	<li><a target="_blank" href="http://jasperreports.sourceforge.net/api/net/sf/jasperreports/engine/export/JRPdfExporter.html#exportReport%28%29">JRPdfExporter</a></li>
</ul>
