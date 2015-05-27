package ie.teamchile.smartapp.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by user on 5/26/15.
 */
public class ApiRootModel {
    private static ApiRootModel instance;
    @Expose
    private Error errors;
    private LoginJson login;
    @Expose
    private List<Appointment> appointments = new ArrayList<>();
    @SerializedName("service_options")
    @Expose
    private List<ServiceOption> serviceOptions = new ArrayList<>();
    private Map<Integer, ServiceOption> serviceOptionsMap;
    @SerializedName("service_users")
    @Expose
    private List<ServiceUser> serviceUsers = new ArrayList<>();
    @SerializedName("service_providers")
    @Expose
    private List<ServiceProvider> serviceProviders = new ArrayList<>();
    @Expose
    private List<Pregnancy> pregnancies = new ArrayList<>();
    @Expose
    private List<Baby> babies = new ArrayList<>();
    @Expose
    private List<Announcement> announcements = new ArrayList<>();
    @Expose
    private List<Clinic> clinics = new ArrayList<>();
    private Map<Integer, Clinic> clinicsMap;

    private ApiRootModel() {
    }

    public static synchronized ApiRootModel getInstance() {
        if (instance == null) {
            instance = new ApiRootModel();
        }
        return instance;
    }

    /**
     * @return The errors
     */
    public Error getError() {
        return errors;
    }

    /**
     * @param errors The errors
     */
    public void setError(Error errors) {
        this.errors = errors;
    }

    /**
     * @return The login
     */
    public LoginJson getLogin() {
        return login;
    }

    /**
     * @param login The login
     */
    public void setLogin(LoginJson login) {
        this.login = login;
    }

    /**
     * @return The appointments
     */
    public List<Appointment> getAppointments() {
        return appointments;
    }

    /**
     * @param appointments The appointments
     */
    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    public List<ServiceOption> getServiceOptions() {
        return serviceOptions;
    }

    /**
     * @param serviceOptions The service_options
     */
    public void setServiceOptions(List<ServiceOption> serviceOptions) {
        this.serviceOptions = serviceOptions;
    }

    public Map<Integer, ServiceOption> getServiceOptionsMap() {
        return serviceOptionsMap;
    }

    public void setServiceOptionsMap(Map<Integer, ServiceOption> serviceOptionsMap) {
        this.serviceOptionsMap = serviceOptionsMap;
    }

    /**
     * @return The clinics
     */
    public List<Clinic> getClinics() {
        return clinics;
    }

    /**
     * @param clinics The clinics
     */
    public void setClinics(List<Clinic> clinics) {
        this.clinics = clinics;
    }

    public Map<Integer, Clinic> getClinicsMap() {
        return clinicsMap;
    }

    public void setClinicsMap(Map<Integer, Clinic> clinicsMap) {
        this.clinicsMap = clinicsMap;
    }

    /**
     * @return The service_providers
     */
    public List<ServiceProvider> getServiceProviders() {
        return serviceProviders;
    }

    /**
     * @param serviceProviders The service_providers
     */
    public void setServiceProviders(List<ServiceProvider> serviceProviders) {
        this.serviceProviders = serviceProviders;
    }

    /**
     * @return The pregnancies
     */
    public List<Pregnancy> getPregnancies() {
        return pregnancies;
    }

    /**
     * @param pregnancies The pregnancies
     */
    public void setPregnancies(List<Pregnancy> pregnancies) {
        this.pregnancies = pregnancies;
    }

    /**
     * @return The babies
     */
    public List<Baby> getBabies() {
        return babies;
    }

    /**
     * @param babies The babies
     */
    public void setBabies(List<Baby> babies) {
        this.babies = babies;
    }

    /**
     * @return The announcements
     */
    public List<Announcement> getAnnouncements() {
        return announcements;
    }

    /**
     * @param announcements The announcements
     */

    public void setAnnouncements(List<Announcement> announcements) {
        this.announcements = announcements;
    }
}
