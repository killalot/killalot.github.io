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